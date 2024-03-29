package org.jax.isopret.gui.service;

import javafx.concurrent.Task;
import org.jax.isopret.core.GoAnalysisResults;
import org.jax.isopret.core.IsopretGoAnalysisRunner;
import org.jax.isopret.core.IsopretProvider;
import org.jax.isopret.core.analysis.IsopretStats;
import org.jax.isopret.core.impl.rnaseqdata.RnaSeqAnalysisMethod;
import org.jax.isopret.core.impl.go.*;
import org.jax.isopret.core.impl.rnaseqdata.IsoformSpecificThresholder;
import org.jax.isopret.core.impl.rnaseqdata.RnaSeqResultsParser;
import org.jax.isopret.data.AccessionNumber;
import org.jax.isopret.data.GoMethod;
import org.jax.isopret.data.MtcMethod;
import org.jax.isopret.data.Transcript;
import org.jax.isopret.model.*;
import org.jax.isopret.core.impl.hgnc.HgncParser;
import org.jax.isopret.core.InterproMapper;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.monarchinitiative.phenol.analysis.stats.GoTerm2PValAndCounts;
import org.monarchinitiative.svart.assembly.GenomicAssemblies;
import org.monarchinitiative.svart.assembly.GenomicAssembly;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.File;
import java.util.*;

/**
 * This class organizes data input and preparation
 * @author Peter N Robinson
 */
public class IsopretDataLoadTask extends Task<Integer>  {
    private final Logger LOGGER = LoggerFactory.getLogger(IsopretDataLoadTask.class);

    /** isopret only supports hg38. */
    private final GenomicAssembly assembly = GenomicAssemblies.GRCh38p13();

    private Ontology geneOntology;

    private Map<AccessionNumber, GeneModel> geneSymbolToModelMap = Map.of();

    /** Key: transcript id; value: set of Annotating GO Terms. */
    private Map<TermId, Set<TermId>> transcript2GoMap = Map.of();

    private InterproMapper interproMapper = null;

    private IsoformSpecificThresholder isoformSpecificThresholder = null;

    //private final File downloadDirectory;
    /**
     * The HBA-DEALS or edgeR analysis file.
     */
    private final File rnaSeqResultsFile;

    private final GoMethod overrepMethod;
    private final MtcMethod multipleTestingMethod;


    private List<GoTerm2PValAndCounts> dgeResults = List.of();
    private List<GoTerm2PValAndCounts> dasResults = List.of();


    IsopretAssociationContainer transcriptContainer = null;
    IsopretAssociationContainer geneContainer = null;

    private final List<String> errors;

    private final IsopretStats.Builder isopretStatsBuilder;

    private final IsopretProvider provider;

    private final RnaSeqAnalysisMethod rnaSeqAnalysisMethod;

    public IsopretDataLoadTask(File downloadDirectory,
                               File hbaDealsFile,
                               GoMethod goMethod,
                               MtcMethod mtcMethod,
                               RnaSeqAnalysisMethod rnaSeqMethod) {
        errors = new ArrayList<>();
        this.provider = IsopretProvider.provider(downloadDirectory.toPath());
        this.rnaSeqResultsFile = hbaDealsFile;
        this.overrepMethod = goMethod;
        this.multipleTestingMethod = mtcMethod;
        this.rnaSeqAnalysisMethod = rnaSeqMethod;
        isopretStatsBuilder = new IsopretStats.Builder();
    }

    public IsoformSpecificThresholder getIsoformSpecificThresholder() {
        return isoformSpecificThresholder;
    }

    public Map<TermId, Set<TermId>> getTranscript2GoMap() {
        return transcript2GoMap;
    }

    @Override
    protected Integer call() {
        updateProgress(0, 1); /* this will update the progress bar */
        updateMessage("Reading Gene Ontology file");
        updateProgress(0.05, 1);

        this.geneOntology = provider.geneOntology();
        updateMessage("Loaded Gene Ontology file");
        updateProgress(0.15, 1);
        updateProgress(0.20, 1);
        Map<GeneSymbolAccession, List<Transcript>> geneSymbolAccessionListMap = provider.geneSymbolToTranscriptListMap();


        updateMessage(String.format("Loaded JannovarReader with %d genes.", geneSymbolAccessionListMap.size()));
        updateProgress(0.25, 1); /* this will update the progress bar */
        updateMessage(String.format("Loaded geneSymbolAccessionListMap with %d gene symbols.", geneSymbolAccessionListMap.size()));
        isopretStatsBuilder.geneSymbolCount(geneSymbolAccessionListMap.size());
        int n_transcripts = geneSymbolAccessionListMap.values()
                .stream()
                .map(List::size)
                .reduce(0, Integer::sum);
        isopretStatsBuilder.transcriptsCount(n_transcripts);
        isopretStatsBuilder.rnaSeqMethod(rnaSeqAnalysisMethod);

        File isoformFunctionFileMf = provider.isoformFunctionListMf().toFile();
        isopretStatsBuilder.info("isoform function file", isoformFunctionFileMf.getAbsolutePath());
        this.transcript2GoMap = provider.transcriptIdToGoTermsMap();
        updateProgress(0.40, 1);
        updateMessage(String.format("Loaded isoformFunctionFileMf (%d transcripts).", transcript2GoMap.size()));
        Map<TermId, Set<TermId>> gene2GoMap = provider.gene2GoMap();
        LOGGER.info("Loaded gene2GoMap with {} entries", gene2GoMap.size());
        isopretStatsBuilder.annotatedGeneCount(gene2GoMap.size());
        IsopretContainerFactory isoContainerFac = new IsopretContainerFactory(geneOntology, transcript2GoMap, gene2GoMap);
        transcriptContainer = isoContainerFac.transcriptContainer();
        geneContainer = isoContainerFac.geneContainer();
        updateProgress(0.55, 1);
        LOGGER.info("Loaded gene container with {} annotating terms", geneContainer.getAnnotatingTermCount());
        isopretStatsBuilder.annotatingGoTermCountGenes(geneContainer.getAnnotatingTermCount());
        isopretStatsBuilder.annotatedGeneCount(geneContainer.getAnnotatedDomainItemCount());
        LOGGER.info("Loaded transcript container with {} annotating terms", transcriptContainer.getAnnotatingTermCount());
        isopretStatsBuilder.annotatingGoTermCountTranscripts(transcriptContainer.getAnnotatingTermCount());
        isopretStatsBuilder.annotatedTranscripts(transcriptContainer.getAnnotatedDomainItemCount());


        File hgncFile = provider.hgncCompleteSet().toFile();
        HgncParser hgncParser = new HgncParser(hgncFile, geneSymbolAccessionListMap);
        this.geneSymbolToModelMap = hgncParser.ensemblMap();
        updateProgress(0.65, 1); /* this will update the progress bar */
        updateMessage(String.format("Loaded Ensembl HGNC map with %d genes", geneSymbolToModelMap.size()));
        LOGGER.info(String.format("Loaded Ensembl HGNC map with %d genes", geneSymbolToModelMap.size()));
        isopretStatsBuilder.hgncCount(geneSymbolToModelMap.size());
        File interproDescriptionFile = provider.interproDomainDesc().toFile();
        File interproDomainsFile = provider.interproDomains().toFile();
        isopretStatsBuilder.info("Interpro domains file", interproDomainsFile.getAbsolutePath());
        isopretStatsBuilder.info("Interpro description file", interproDescriptionFile.getAbsolutePath());
        this.interproMapper = provider.interproMapper();
        updateProgress(0.70, 1); /* this will update the progress bar */
        updateMessage(String.format("Loaded InterproMapper with %d descriptions", interproMapper.getInterproDescriptionCount()));
        isopretStatsBuilder.interproDescriptionCount(interproMapper.getInterproDescriptionCount());
        isopretStatsBuilder.interproAnnotationCount(interproMapper.getInterproAnnotationCount());
        LOGGER.info(String.format("Loaded InterproMapper with %d descriptions", interproMapper.getInterproDescriptionCount()));
        updateProgress(0.80, 1); /* this will update the progress bar */
        Map<AccessionNumber, GeneResult> geneResultsMap =
                RnaSeqResultsParser.parse(this.rnaSeqResultsFile, geneSymbolToModelMap, rnaSeqAnalysisMethod);
        updateProgress(0.85, 1); /* this will update the progress bar */
        updateMessage(String.format("Loaded HBA-DEALS results with %d observed genes.", geneResultsMap.size()));
        if (this.rnaSeqAnalysisMethod == RnaSeqAnalysisMethod.HBADEALS) {
            this.isoformSpecificThresholder = IsoformSpecificThresholder.fromHbaDeals(geneResultsMap,
                    0.05,
                    geneContainer,
                    transcriptContainer);
        } else {
            this.isoformSpecificThresholder = IsoformSpecificThresholder.fromEdgeR(geneResultsMap,
                    0.05,
                    geneContainer,
                    transcriptContainer);
        }
        updateProgress(0.90, 1);
        updateMessage("Finished loading data for isopret analysis.");
        LOGGER.info("Beginning DGE GO analysis");
        isopretStatsBuilder.dasIsoformCount(isoformSpecificThresholder.getDasIsoformCount());
        isopretStatsBuilder.dgeGeneCount(isoformSpecificThresholder.getDgeGeneCount());
        isopretStatsBuilder.dasStudy(isoformSpecificThresholder.getDasStudy().getAnnotatedItemCount());
        isopretStatsBuilder.dasPopulation(isoformSpecificThresholder.getDasPopulation().getAnnotatedItemCount());
        isopretStatsBuilder.dgeStudy(isoformSpecificThresholder.getDgeStudy().getAnnotatedItemCount());
        isopretStatsBuilder.dgePopulation(isoformSpecificThresholder.getDgePopulation().getAnnotatedItemCount());
        isopretStatsBuilder.fdrThreshold(isoformSpecificThresholder.getFdrThreshold());
        isopretStatsBuilder.expressionPthreshold(isoformSpecificThresholder.getExpressionPepThreshold());
        isopretStatsBuilder.splicingPthreshold(isoformSpecificThresholder.getSplicingPepThreshold());
        IsopretGoAnalysisRunner runner;
        if (this.rnaSeqAnalysisMethod == RnaSeqAnalysisMethod.HBADEALS) {
            runner = IsopretGoAnalysisRunner.hbadeals(provider,
                    this.rnaSeqResultsFile,
                    multipleTestingMethod,
                    overrepMethod);
        } else {
            runner = IsopretGoAnalysisRunner.edgeR(provider,
                    this.rnaSeqResultsFile,
                    multipleTestingMethod,
                    overrepMethod);
        }
        updateMessage("Running overrepresentation analysis");
        updateProgress(0.95, 1);
        GoAnalysisResults goResults = runner.run();
        this.dgeResults = goResults.dgeGoTerms();
        this.dasResults = goResults.dasGoTerms();
        updateProgress(0.97, 1);
        updateMessage(String.format("Finished DAS/DGE overrepresentation analysis. %d/%d overrepresented GO Terms",
                this.dasResults.size(),dgeResults.size()));
        updateProgress(1.0, 1);
        updateMessage("Done");
        return 0;
    }

    public List<GoTerm2PValAndCounts> getDgeResults() {
        return dgeResults;
    }

    public List<GoTerm2PValAndCounts> getDasResults() {
        return dasResults;
    }

    Map<TermId, TermId> createTranscriptToGeneIdMap(Map<GeneSymbolAccession, List<Transcript>> gene2transcript) {
        Map<TermId, TermId> accessionNumberMap = new HashMap<>();
        for (var entry : gene2transcript.entrySet()) {
            var geneAcc = entry.getKey();
            var geneTermId = geneAcc.accession().toTermId();
            var transcriptList = entry.getValue();
            for (var transcript: transcriptList) {
                var transcriptAcc = transcript.accessionId();
                var transcriptTermId = transcriptAcc.toTermId();
                accessionNumberMap.put(transcriptTermId, geneTermId);
            }
        }
        return Map.copyOf(accessionNumberMap); // immutable copy
    }

    public Ontology getGeneOntology() {
        return geneOntology;
    }

    public InterproMapper getInterproMapper() {
        return interproMapper;
    }

    public List<String> getErrors() {
        return errors;
    }

    public IsopretAssociationContainer getTranscriptContainer() {
        return transcriptContainer;
    }

    public IsopretAssociationContainer getGeneContainer() {
        return geneContainer;
    }

    public IsopretStats getIsopretStats() { return isopretStatsBuilder.build(); }

    public GoMethod getOverrepMethod() {
        return overrepMethod;
    }

    public MtcMethod getMultipleTestingMethod() {
        return multipleTestingMethod;
    }

    public Map<AccessionNumber, GeneModel> getGeneSymbolToModelMap() {
        return geneSymbolToModelMap;
    }
}
