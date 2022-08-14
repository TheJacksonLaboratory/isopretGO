package org.jax.isopret.core.impl.go;

import org.jax.isopret.core.GoAnalysisResults;
import org.jax.isopret.core.IsopretGoAnalysisRunner;
import org.jax.isopret.core.IsopretProvider;
import org.jax.isopret.core.impl.rnaseqdata.HbaDealsIsoformSpecificThresholder;
import org.jax.isopret.core.impl.rnaseqdata.HbaDealsParser;
import org.jax.isopret.core.impl.rnaseqdata.HbaDealsResult;
import org.jax.isopret.except.IsopretRuntimeException;
import org.jax.isopret.model.*;
import org.monarchinitiative.phenol.analysis.AssociationContainer;
import org.monarchinitiative.phenol.analysis.StudySet;
import org.monarchinitiative.phenol.analysis.stats.*;
import org.monarchinitiative.phenol.analysis.stats.mtc.*;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class DefaultIsopretGoAnalysisRunner implements IsopretGoAnalysisRunner {

    private final static Logger LOGGER = LoggerFactory.getLogger(DefaultIsopretGoAnalysisRunner.class);

    private final String hbaDealsFile;
    private final MtcMethod mtcMethod;
    private final GoMethod goMethod;
    private final IsopretProvider provider;

    public DefaultIsopretGoAnalysisRunner(IsopretProvider provider , String hbaDealsFile, MtcMethod mtcMethod, GoMethod goMethod) {
        this.provider = provider;
        this.hbaDealsFile = hbaDealsFile;
        this.mtcMethod = mtcMethod;
        this.goMethod = goMethod;
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
        LOGGER.info("About to create thresholder");
        HbaDealsParser hbaParser = new HbaDealsParser(this.hbaDealsFile, hgncMap);
        Map<AccessionNumber, HbaDealsResult> hbaDealsResults = hbaParser.getEnsgAcc2hbaDealsMap();
        LOGGER.trace("Analyzing {} genes.", hbaDealsResults.size());
        HbaDealsIsoformSpecificThresholder isoThresholder = new HbaDealsIsoformSpecificThresholder(hbaDealsResults,
                0.05,
                geneContainer,
                transcriptContainer);
        LOGGER.info("Initialized HBADealsThresholder");
        LOGGER.info("isoThresholder.getDgePopulation().getAnnotatedItemCount()={}"
                ,isoThresholder.getDgePopulation().getAnnotatedItemCount());
        /* ---------- 7. Set up HbaDeal GO analysis ------------------------- */
        LOGGER.info("Using Gene Ontology approach {}", goMethod.name());
        LOGGER.info("About to create HbaDealsGoContainer");
        List<GoTerm2PValAndCounts> dgeGoTerms = doGoAnalysis(goMethod,
                mtcMethod,
                geneOntology,
                isoThresholder.getDgeStudy(),
                isoThresholder.getDgePopulation());
        System.out.println("Go enrichments, DGE");
        for (var cts : dgeGoTerms) {
            if (cts.passesThreshold(0.05))
                try {
                    System.out.println(cts.getRow(geneOntology));
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
        System.out.println("Go enrichments, DAS");
        for (var cts : dasGoTerms) {
            if (cts.passesThreshold(0.05))
                try {
                    System.out.println(cts.getRow(geneOntology));
                } catch (Exception e) {
                    // some issue with getting terms, probably ontology is not in sync
                    LOGGER.error("Could not get data for {}: {}", cts, e.getLocalizedMessage());
                }
        }
        return new DefaultGoAnalysisResults(hbaDealsFile, mtcMethod, goMethod, dasGoTerms, dgeGoTerms);
    }



    private List<GoTerm2PValAndCounts> doGoAnalysis(GoMethod goMethod, MtcMethod mtcMethod, Ontology geneOntology, StudySet dgeStudy, StudySet dgePopulation) {
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
                    dgePopulation,
                    dgeStudy,
                    mtc);
        } else if (goMethod.equals(GoMethod.PCunion)) {
            pvalcal = new ParentChildUnionPValueCalculation(geneOntology,
                    dgePopulation,
                    dgeStudy,
                    mtc);
        } else if (goMethod.equals(GoMethod.PCintersect)) {
            pvalcal = new ParentChildIntersectionPValueCalculation(geneOntology,
                    dgePopulation,
                    dgeStudy,
                    mtc);
        } else {
            throw new IsopretRuntimeException("Did not recognise GO Method");
        }
        return pvalcal.calculatePVals()
                .stream()
                .filter(item -> item.passesThreshold(ALPHA))
                .sorted()
                .collect(Collectors.toList());

    }
}
