package org.jax.isopret.core.impl.go;

import org.jax.isopret.core.GoAnalysisResults;
import org.jax.isopret.core.IsopretGoAnalysisRunner;
import org.jax.isopret.core.IsopretProvider;
import org.jax.isopret.core.impl.rnaseqdata.IsoformSpecificThresholder;
import org.jax.isopret.core.impl.rnaseqdata.RnaSeqAnalysisMethod;
import org.jax.isopret.core.impl.rnaseqdata.RnaSeqResultsParser;
import org.jax.isopret.data.AccessionNumber;
import org.jax.isopret.data.GoMethod;
import org.jax.isopret.data.MtcMethod;
import org.jax.isopret.data.Transcript;
import org.jax.isopret.exception.IsopretRuntimeException;
import org.jax.isopret.model.*;
import org.monarchinitiative.phenol.analysis.AssociationContainer;
import org.monarchinitiative.phenol.analysis.StudySet;
import org.monarchinitiative.phenol.analysis.stats.*;
import org.monarchinitiative.phenol.analysis.stats.mtc.*;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class DefaultIsopretGoAnalysisRunner implements IsopretGoAnalysisRunner {

    private final static Logger LOGGER = LoggerFactory.getLogger(DefaultIsopretGoAnalysisRunner.class);

    private final File rnaSeqResultsFile;
    private final MtcMethod mtcMethod;
    private final GoMethod goMethod;
    private final IsopretProvider provider;
    /** Threshold for considering differential expression or splicing in HBA-DEALS or edgeR */
    private final double fdrThreshold = 0.05;
    /** Multiple testing p-value threshold for Gene Ontology Overrepresentation analysis. */
    private final double alphaThreshold = 0.05;

    private boolean exportAll = false;

    private final RnaSeqAnalysisMethod rnaSeqAnalysisMethod;

    public DefaultIsopretGoAnalysisRunner(IsopretProvider provider,
                                          File rnaSeqDataFile,
                                          MtcMethod mtcMethod,
                                          GoMethod goMethod,
                                          RnaSeqAnalysisMethod rnaSeqMethod) {
        this.provider = provider;
        this.rnaSeqResultsFile = rnaSeqDataFile;
        if (! rnaSeqResultsFile.isFile()) {
            throw new IsopretRuntimeException("Could not find HBA-DEALS file at " + rnaSeqDataFile);
        }
        this.mtcMethod = mtcMethod;
        this.goMethod = goMethod;
        this.rnaSeqAnalysisMethod = rnaSeqMethod;
    }


    @Override
    public GoAnalysisResults run() {
        Ontology geneOntology = provider.geneOntology();
        Map<GeneSymbolAccession, List<Transcript>>  geneSymbolAccessionListMap = provider.geneSymbolToTranscriptListMap();
        Map<AccessionNumber, GeneModel> hgncMap  = provider.ensemblGeneModelMap();
        Map<GeneSymbolAccession, List<Transcript>> geneSymbolAccessionToTranscriptMap = provider.geneSymbolToTranscriptListMap();
        Map<TermId, Set<TermId>> transcriptIdToGoTermsMap = provider.transcriptIdToGoTermsMap();
        AssociationContainer<TermId> transcriptContainer = provider.transcriptContainer();
        AssociationContainer<TermId> geneContainer = provider.geneContainer();

        // ----------  6. HBA-DEALS input file  ----------------
        LOGGER.info("About to create thresholder for RNA-seq method {}", rnaSeqAnalysisMethod);
        Map<AccessionNumber, GeneResult> geneResults;
        if (rnaSeqAnalysisMethod == RnaSeqAnalysisMethod.HBADEALS) {
            geneResults = RnaSeqResultsParser.fromHbaDeals(this.rnaSeqResultsFile, hgncMap);
        } else {
            geneResults = RnaSeqResultsParser.fromEdgeR(this.rnaSeqResultsFile, hgncMap);
        }
        LOGGER.trace("Analyzing {} genes.", geneResults.size());
        IsoformSpecificThresholder isoThresholder;
        if (rnaSeqAnalysisMethod == RnaSeqAnalysisMethod.HBADEALS) {
            isoThresholder = IsoformSpecificThresholder.fromHbaDeals(geneResults,
                    fdrThreshold,
                    geneContainer,
                    transcriptContainer);
        } else {
            isoThresholder = IsoformSpecificThresholder.fromEdgeR(geneResults,
                    fdrThreshold,
                    geneContainer,
                    transcriptContainer);
        }
        LOGGER.info("Initialized HBADealsThresholder");
        LOGGER.info("isoThresholder.getDgePopulation().getAnnotatedItemCount()={}",
                isoThresholder.getDgePopulation().getAnnotatedItemCount());
        /* ---------- 7. Set up HbaDeal GO analysis ------------------------- */
        LOGGER.info("Using Gene Ontology approach {}", goMethod.name());
        LOGGER.info("About to create HbaDealsGoContainer");
        List<GoTerm2PValAndCounts> dgeGoTerms = doGoAnalysis(goMethod,
                mtcMethod,
                geneOntology,
                isoThresholder.getDgeStudy(),
                isoThresholder.getDgePopulation());
        LOGGER.trace("Go enrichments, DGE");
        for (var cts : dgeGoTerms) {
            if (cts.passesThreshold(alphaThreshold))
                try {
                    LOGGER.trace(cts.getRow(geneOntology));
                } catch (Exception e) {
                    // some issue with getting terms, probably ontology is not in sync
                    LOGGER.error("Could not get data for {}: {}", cts, e.getLocalizedMessage());
                }
        }

        List<GoTerm2PValAndCounts> dasGoTerms = doGoAnalysis(goMethod,
                mtcMethod,
                geneOntology,
                isoThresholder.getDasStudy(),
                isoThresholder.getDasPopulation());
        LOGGER.trace("Go enrichments, DAS");
        for (var cts : dasGoTerms) {
            if (cts.passesThreshold(alphaThreshold))
                try {
                    LOGGER.trace(cts.getRow(geneOntology));
                } catch (Exception e) {
                    // some issue with getting terms, probably ontology is not in sync
                    LOGGER.error("Could not get data for {}: {}", cts, e.getLocalizedMessage());
                }
        }
        return new DefaultGoAnalysisResults(rnaSeqResultsFile, mtcMethod, goMethod, dasGoTerms, dgeGoTerms);
    }

    @Override
    public void exportAll() {
        this.exportAll = true;
    }


    /**
     * This method is used to perform GO analysis for either DGE or DAS
     * @param goMethod One of the {@link GoMethod}s, such as TermForTerm
     * @param mtcMethod One of the {@link MtcMethod}s, such as Bonferroni
     * @param geneOntology Link to the phenol {@link Ontology} object for Gene Ontology
     * @param studySet The set of differentially expressed genes (or the set of differentially spliced isoforms)
     * @param populationSet The set of all considered genes (or isoforms)
     * @return list of GO Overrepresentation analysis results
     */
    private List<GoTerm2PValAndCounts> doGoAnalysis(GoMethod goMethod,
                                                    MtcMethod mtcMethod,
                                                    Ontology geneOntology,
                                                    StudySet studySet,
                                                    StudySet populationSet) {
        final double ALPHA = 0.05;
        PValueCalculation pvalcal;
        MultipleTestingCorrection mtc;
        switch (mtcMethod) {
            case BONFERRONI -> mtc = new Bonferroni();
            case BONFERRONI_HOLM -> mtc = new BonferroniHolm();
            case BENJAMINI_HOCHBERG -> mtc = new BenjaminiHochberg();
            case BENJAMINI_YEKUTIELI -> mtc = new BenjaminiYekutieli();
            case SIDAK -> mtc = new Sidak();
            case NONE -> mtc = new NoMultipleTestingCorrection();
            default -> {
                // should never happen
                System.err.println("[WARNING] Did not recognize MTC");
                mtc = new Bonferroni();
            }
        }
        if (goMethod.equals(GoMethod.TFT)) {
            pvalcal = new TermForTermPValueCalculation(geneOntology,
                    populationSet,
                    studySet,
                    mtc);
        } else if (goMethod.equals(GoMethod.PCunion)) {
            pvalcal = new ParentChildUnionPValueCalculation(geneOntology,
                    populationSet,
                    studySet,
                    mtc);
        } else if (goMethod.equals(GoMethod.PCintersect)) {
            pvalcal = new ParentChildIntersectionPValueCalculation(geneOntology,
                    populationSet,
                    studySet,
                    mtc);
        } else {
            throw new IsopretRuntimeException("Did not recognise GO Method");
        }
        if (this.exportAll) {
            LOGGER.info("Returning GO Overrepresentation results with no p-value threshold");
            return pvalcal.calculatePVals()
                    .stream()
                    .sorted()
                    .collect(Collectors.toList());
        } else {
            LOGGER.info("Returning GO Overrepresentation results with p-value threshold of {}", ALPHA);
            return pvalcal.calculatePVals()
                    .stream()
                    .filter(item -> item.passesThreshold(ALPHA))
                    .sorted()
                    .collect(Collectors.toList());
        }
    }
}
