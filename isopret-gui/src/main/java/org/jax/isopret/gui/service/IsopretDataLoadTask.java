package org.jax.isopret.gui.service;

import javafx.concurrent.Task;
import org.jax.isopret.core.analysis.IsopretStats;
import org.jax.isopret.core.except.IsopretRuntimeException;
import org.jax.isopret.core.go.*;
import org.jax.isopret.core.hbadeals.HbaDealsIsoformSpecificThresholder;
import org.jax.isopret.core.hbadeals.HbaDealsParser;
import org.jax.isopret.core.hbadeals.HbaDealsResult;
import org.jax.isopret.core.hgnc.HgncItem;
import org.jax.isopret.core.hgnc.HgncParser;
import org.jax.isopret.core.interpro.InterproMapper;
import org.jax.isopret.core.io.TranscriptFunctionFileParser;
import org.jax.isopret.core.transcript.AccessionNumber;
import org.jax.isopret.core.transcript.JannovarReader;
import org.jax.isopret.core.transcript.Transcript;
import org.monarchinitiative.phenol.io.OntologyLoader;
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

    private Map<AccessionNumber, List<Transcript>> geneIdToTranscriptMap;

    private Map<AccessionNumber, HgncItem> hgncMap = Map.of();
    /** Key ensembl transcript id; values: annotating go terms .*/
    private Map<TermId, Set<TermId>> transcriptToGoMap = Map.of();

    private Map<String, List<Transcript>> geneSymbolToTranscriptMap = Map.of();
    /** Key: transcript id; value: set of Annotating GO Terms. */
    private Map<TermId, Set<TermId>> transcript2GoMap = Map.of();

    private InterproMapper interproMapper = null;

    private HbaDealsIsoformSpecificThresholder isoformSpecificThresholder = null;

    private final File downloadDirectory;

    private final File hbaDealsFile;

    private final GoMethod overrepMethod;
    private final MtcMethod multipleTestingMethod;


    private List<GoTerm2PValAndCounts> dgeResults = List.of();
    private List<GoTerm2PValAndCounts> dasResults = List.of();


    IsopretAssociationContainer transcriptContainer = null;
    IsopretAssociationContainer geneContainer = null;

    private final List<String> errors;

    private final IsopretStats.Builder isopretStatsBuilder;

    public IsopretDataLoadTask(File downloadDirectory, File hbaDealsFile, GoMethod goMethod, MtcMethod mtcMethod) {
        errors = new ArrayList<>();
        this.downloadDirectory = downloadDirectory;
        this.hbaDealsFile = hbaDealsFile;
        this.overrepMethod = goMethod;
        this.multipleTestingMethod = mtcMethod;
        isopretStatsBuilder = new IsopretStats.Builder();
    }

    public HbaDealsIsoformSpecificThresholder getIsoformSpecificThresholder() {
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

        File goJsonFile = new File(downloadDirectory + File.separator + "go.json");
        if (!goJsonFile.isFile()) {
            throw new IsopretRuntimeException("Could not find Gene Ontology JSON file at " + goJsonFile.getAbsolutePath());
        }
        isopretStatsBuilder.info("GO file", goJsonFile.getAbsolutePath());
        this.geneOntology = OntologyLoader.loadOntology(goJsonFile);
        updateMessage("Loaded Gene Ontology file");
        updateProgress(0.15, 1);
        File jannovarFile = new File(downloadDirectory + File.separator + "hg38_ensembl.ser");
        if (! jannovarFile.isFile()) {
            String errorMsg = String.format("Could not find hg38_ensembl.ser (Jannovar file) at \"%s\"",
                    jannovarFile.getAbsolutePath());
            throw new IsopretRuntimeException(errorMsg);
        }
        isopretStatsBuilder.info("Jannovar file", jannovarFile.getAbsolutePath());
        JannovarReader jannovarReader = new JannovarReader(jannovarFile, assembly);

        updateProgress(0.20, 1);
        updateMessage(String.format("Loaded JannovarReader with %d gene symbols.", jannovarReader.getSymbolToTranscriptListMap().size()));
        LOGGER.info(String.format("Loaded JannovarReader with %d gene symbols.", jannovarReader.getSymbolToTranscriptListMap().size()));
        this.geneIdToTranscriptMap = jannovarReader.getGeneIdToTranscriptMap();
        this.geneSymbolToTranscriptMap = jannovarReader.getSymbolToTranscriptListMap();
        updateProgress(0.25, 1); /* this will update the progress bar */
        updateMessage(String.format("Loaded geneIdToTranscriptMap with %d gene symbols.", geneIdToTranscriptMap.size()));
        LOGGER.info(String.format("Loaded geneIdToTranscriptMap with %d gene symbols.", geneIdToTranscriptMap.size()));
        isopretStatsBuilder.geneSymbolCount(geneIdToTranscriptMap.size());
        int n_transcripts = geneIdToTranscriptMap.values()
                .stream()
                .map(List::size)
                .reduce(0, Integer::sum);
        isopretStatsBuilder.transcriptsCount(n_transcripts);


        File isoformFunctionFile = new File(downloadDirectory + File.separator + "isoform_function_list_mf.txt");
        if (! isoformFunctionFile.isFile()) {
            throw new IsopretRuntimeException("Could not find \"isoform_function_list_mf.txt\" in download directory");
        } else {
            isopretStatsBuilder.info("isoform function file", isoformFunctionFile.getAbsolutePath());
            TranscriptFunctionFileParser fxnparser = new TranscriptFunctionFileParser(downloadDirectory, geneOntology);
            Map<TermId, TermId> transcriptToGeneIdMap = createTranscriptToGeneIdMap(this.geneIdToTranscriptMap);
            this.transcript2GoMap = fxnparser.getTranscriptIdToGoTermsMap();
            updateProgress(0.40, 1);
            updateMessage(String.format("Loaded isoformFunctionFile (%d transcripts).", transcript2GoMap.size()));

            Map<TermId, Set<TermId>> gene2GoMap = fxnparser.getGeneIdToGoTermsMap(transcriptToGeneIdMap);
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

        }
        File hgncFile = new File(downloadDirectory + File.separator + "hgnc_complete_set.txt");
        HgncParser hgncParser = new HgncParser(hgncFile);
        this.hgncMap = hgncParser.ensemblMap();
        updateProgress(0.65, 1); /* this will update the progress bar */
        updateMessage(String.format("Loaded Ensembl HGNC map with %d genes", hgncMap.size()));
        LOGGER.info(String.format("Loaded Ensembl HGNC map with %d genes", hgncMap.size()));
        isopretStatsBuilder.hgncCount(hgncMap.size());

        File interproDescriptionFile = new File(downloadDirectory + File.separator + "interpro_domain_desc.txt");
        File interproDomainsFile = new File(downloadDirectory + File.separator + "interpro_domains.txt");
        if (! interproDomainsFile.isFile()) {
            throw new IsopretRuntimeException("Could not find interpro_domains.txt at " +
                    interproDomainsFile.getAbsolutePath());
        }
        if (! interproDescriptionFile.isFile()) {
            throw new IsopretRuntimeException("Could not find interpro_domain_desc.txt at " +
                    interproDescriptionFile.getAbsolutePath());
        }
        isopretStatsBuilder.info("Interpro domains file", interproDomainsFile.getAbsolutePath());
        isopretStatsBuilder.info("Interpro description file", interproDescriptionFile.getAbsolutePath());
        this.interproMapper = new InterproMapper(interproDescriptionFile, interproDomainsFile);
        updateProgress(0.70, 1); /* this will update the progress bar */
        updateMessage(String.format("Loaded InterproMapper with %d descriptions", interproMapper.getInterproDescriptionCount()));
        isopretStatsBuilder.interproDescriptionCount(interproMapper.getInterproDescriptionCount());
        isopretStatsBuilder.interproAnnotationCount(interproMapper.getInterproAnnotationCount());
        LOGGER.info(String.format("Loaded InterproMapper with %d descriptions", interproMapper.getInterproDescriptionCount()));

        TranscriptFunctionFileParser parser = new TranscriptFunctionFileParser(downloadDirectory, geneOntology);
        transcriptToGoMap = parser.getTranscriptIdToGoTermsMap();
        updateProgress(0.80, 1); /* this will update the progress bar */
        updateMessage(String.format("Loaded transcriptToGoMap with %d elements", transcriptToGoMap.size()));
        LOGGER.info(String.format("Loaded transcriptToGoMap with %d elements", transcriptToGoMap.size()));
        HbaDealsParser hbaParser = new HbaDealsParser(this.hbaDealsFile.getAbsolutePath(), hgncMap);
        Map<String, HbaDealsResult> hbaDealsResults = hbaParser.getHbaDealsResultMap();
        updateProgress(0.90, 1); /* this will update the progress bar */
        updateMessage(String.format("Loaded HBA-DEALS results with %d observed genes.", hbaDealsResults.size()));
        this.isoformSpecificThresholder = new HbaDealsIsoformSpecificThresholder(hbaDealsResults,
                0.05,
                geneContainer,
                transcriptContainer);
        updateProgress(0.95, 1);
        updateMessage(String.format("Loaded transcriptToGoMap with %d elements", transcriptToGoMap.size()));
        LOGGER.info(String.format("Loaded transcriptToGoMap with %d elements", transcriptToGoMap.size()));
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

        HbaDealsGoAnalysis dgeGoAnalysis = new HbaDealsGoAnalysis(geneOntology,
                isoformSpecificThresholder.getDgeStudy(),
                isoformSpecificThresholder.getDgePopulation(),
                this.overrepMethod,
                this.multipleTestingMethod);
        this.dgeResults = dgeGoAnalysis.overrepresetationAnalysis();

        updateProgress(0.97, 1);
        updateMessage(String.format("Finished DGE overrepresentation analysis. %d overrepresented GO Terms",
               dgeResults.size()));
        LOGGER.info("Finished DGE GO analysis, n = {}", this.dgeResults.size());
        LOGGER.info("Beginning DAS GO analysis");
        HbaDealsGoAnalysis dasGoAnalysis = new HbaDealsGoAnalysis(geneOntology,
                isoformSpecificThresholder.getDasStudy(),
                isoformSpecificThresholder.getDasPopulation(),
                this.overrepMethod,
                this.multipleTestingMethod);
        this.dasResults = dasGoAnalysis.overrepresetationAnalysis();
        LOGGER.info("Finished DAS GO analysis, n = {}", this.dasResults.size());
        updateProgress(0.95, 1);
        updateMessage(String.format("Finished DAS overrepresentation analysis. %d overrepresented GO Terms",
                dasResults.size()));
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

    Map<TermId, TermId> createTranscriptToGeneIdMap(Map<AccessionNumber, List<Transcript>> gene2transcript) {
        Map<TermId, TermId> accessionNumberMap = new HashMap<>();
        for (var entry : gene2transcript.entrySet()) {
            var geneAcc = entry.getKey();
            var geneTermId = geneAcc.toTermId();
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

    public Map<AccessionNumber, List<Transcript>> getGeneIdToTranscriptMap() {
        return geneIdToTranscriptMap;
    }

    public InterproMapper getInterproMapper() {
        return interproMapper;
    }

    public List<String> getErrors() {
        return errors;
    }

    public Map<String, List<Transcript>> getGeneSymbolToTranscriptMap() {
        return geneSymbolToTranscriptMap;
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
}
