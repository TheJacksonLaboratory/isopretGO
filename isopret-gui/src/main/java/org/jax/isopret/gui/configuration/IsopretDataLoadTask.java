package org.jax.isopret.gui.configuration;

import javafx.application.Platform;
import javafx.concurrent.Task;
import org.jax.isopret.core.go.IsopretGeneAssociationContainer;
import org.jax.isopret.core.except.IsopretRuntimeException;
import org.jax.isopret.core.hbadeals.HbaDealsParser;
import org.jax.isopret.core.hbadeals.HbaDealsResult;
import org.jax.isopret.core.hbadeals.HbaDealsThresholder;
import org.jax.isopret.core.hgnc.HgncItem;
import org.jax.isopret.core.hgnc.HgncParser;
import org.jax.isopret.core.interpro.InterproMapper;
import org.jax.isopret.core.io.TranscriptFunctionFileParser;
import org.jax.isopret.core.transcript.AccessionNumber;
import org.jax.isopret.core.transcript.JannovarReader;
import org.jax.isopret.core.transcript.Transcript;
import org.monarchinitiative.phenol.analysis.AssociationContainer;
import org.monarchinitiative.phenol.analysis.GoAssociationContainer;
import org.monarchinitiative.phenol.analysis.StudySet;
import org.monarchinitiative.phenol.io.OntologyLoader;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.monarchinitiative.svart.GenomicAssemblies;
import org.monarchinitiative.svart.GenomicAssembly;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.File;
import java.util.*;

/**
 * This class organizes data input and preparation
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

    private GoAssociationContainer goAssociationContainer = null;

    private Map<String, List<Transcript>> geneSymbolToTranscriptMap = Map.of();

    private InterproMapper interproMapper = null;

    private  HbaDealsThresholder thresholder = null;

    private final File downloadDirectory;

    private final File hbaDealsFile;

    private StudySet study = null;
    private StudySet population = null;

    AssociationContainer transcriptContainer = null;
    AssociationContainer geneContainer = null;

    private final List<String> errors;

    public IsopretDataLoadTask(File downloadDirectory, File hbaDealsFile) {
        errors = new ArrayList<>();
        this.downloadDirectory = downloadDirectory;
        this.hbaDealsFile = hbaDealsFile;
    }

    private void updateProg(double p, String message) {
        Platform.runLater(() -> {
            updateProgress(p, 1.0);
            updateMessage(message);
        });
    }

    @Override
    protected Integer call() {
        boolean valid = true;
        updateProgress(0, 1); /* this will update the progress bar */
        updateMessage("Reading Gene Ontology file");

        File goJsonFile = new File(downloadDirectory + File.separator + "go.json");
        if (!goJsonFile.isFile()) {
            errors.add("Could not find Gene Ontology JSON file at " + goJsonFile.getAbsolutePath());
            return 1;
        }
        this.geneOntology = OntologyLoader.loadOntology(goJsonFile);

        updateProgress(0.2, 1); /* this will update the progress bar */
        updateMessage(String.format("Loaded Gene Ontology json file with %d terms.", geneOntology.countNonObsoleteTerms()));
        LOGGER.info(String.format("Loaded Gene Ontology json file with %d terms.", geneOntology.countNonObsoleteTerms()));
        File goGafFile = new File(downloadDirectory + File.separator + "goa_human.gaf");
        if (!goGafFile.isFile()) {
            errors.add("Could not find Gene Ontology goa_human.gaf file at " +
                    goGafFile.getAbsolutePath());
            valid = false;
        } else {
            this.goAssociationContainer = GoAssociationContainer.loadGoGafAssociationContainer(goGafFile, geneOntology);
        }


        File isoformFunctionFile = new File(downloadDirectory + File.separator + "isoform_function_list.txt");
        if (! isoformFunctionFile.isFile()) {
            errors.add("Could not find \"isoform_function_list.txt\" in download directory");
            valid = false;
        } else {
            TranscriptFunctionFileParser fxnparser = new TranscriptFunctionFileParser(isoformFunctionFile, geneOntology);
            Map<TermId, TermId> transcriptToGeneIdMap = createTranscriptToGeneIdMap(this.geneIdToTranscriptMap);
            Map<TermId, Set<TermId>> transcript2GoMap = fxnparser.getTranscriptIdToGoTermsMap();
            updateProgress(0.25, 1); /* this will update the progress bar */
            updateMessage(String.format("Loaded isoformFunctionFile %d transcript.", transcript2GoMap.size()));
            Map<TermId, Set<TermId>> gene2GoMap = fxnparser.getGeneIdToGoTermsMap(transcriptToGeneIdMap);
            transcriptContainer = new IsopretGeneAssociationContainer(geneOntology, transcript2GoMap);
            geneContainer = new IsopretGeneAssociationContainer(geneOntology, gene2GoMap);
            LOGGER.info("Loaded gene2GoMap with {} entries", gene2GoMap.size());
        }


        updateProgress(0.45, 1); /* this will update the progress bar */
        updateMessage(String.format("Loaded GO Association container with %d associations",
                            this.goAssociationContainer.getRawAssociations().size()));
        LOGGER.info(String.format("Loaded GO Association container with %d associations",
                this.goAssociationContainer.getRawAssociations().size()));
        File jannovarFile = new File(downloadDirectory + File.separator + "hg38_ensembl.ser");
        if (! jannovarFile.isFile()) {
            errors.add("Could not find hg38_ensembl.ser (Jannovar file) at " +
                    jannovarFile.getAbsolutePath());
            return 1;
        }
        JannovarReader jannovarReader = new JannovarReader(jannovarFile, assembly);

        updateProgress(0.60, 1); /* this will update the progress bar */
        updateMessage(String.format("Loaded JannovarReader with %d gene symbols.", jannovarReader.getSymbolToTranscriptListMap().size()));
        LOGGER.info(String.format("Loaded JannovarReader with %d gene symbols.", jannovarReader.getSymbolToTranscriptListMap().size()));
        this.geneIdToTranscriptMap = jannovarReader.getGeneIdToTranscriptMap();
        this.geneSymbolToTranscriptMap = jannovarReader.getSymbolToTranscriptListMap();

        updateProgress(0.65, 1); /* this will update the progress bar */
        updateMessage(String.format("Loaded geneIdToTranscriptMap with %d gene symbols.", geneIdToTranscriptMap.size()));
        LOGGER.info(String.format("Loaded geneIdToTranscriptMap with %d gene symbols.", geneIdToTranscriptMap.size()));
        HgncParser hgncParser = new HgncParser();
        this.hgncMap = hgncParser.ensemblMap();
        updateProgress(0.70, 1); /* this will update the progress bar */
        updateMessage(String.format("Loaded Ensembl HGNC map with %d genes", hgncMap.size()));
        LOGGER.info(String.format("Loaded Ensembl HGNC map with %d genes", hgncMap.size()));

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
        this.interproMapper = new InterproMapper(interproDescriptionFile, interproDomainsFile);
        updateProgress(0.80, 1); /* this will update the progress bar */
        updateMessage(String.format("Loaded InterproMapper with %d domains", interproMapper.getInterproDescription().size()));

        LOGGER.info(String.format("Loaded InterproMapper with %d domains", interproMapper.getInterproDescription().size()));
        File predictionFile = new File(downloadDirectory + File.separator + "isoform_function_list.txt");
        if (!predictionFile.isFile()) {
            throw new IsopretRuntimeException("Could not find isoform_function_list.txt at " +
                    predictionFile.getAbsolutePath());
        }
        TranscriptFunctionFileParser parser = new TranscriptFunctionFileParser(predictionFile, geneOntology);
        transcriptToGoMap = parser.getTranscriptIdToGoTermsMap();
        updateProgress(0.85, 1); /* this will update the progress bar */
        updateMessage(String.format("Loaded transcriptToGoMap with %d elements", transcriptToGoMap.size()));
        LOGGER.info(String.format("Loaded transcriptToGoMap with %d elements", transcriptToGoMap.size()));
        HbaDealsParser hbaParser = new HbaDealsParser(this.hbaDealsFile.getAbsolutePath(), hgncMap);
        Map<String, HbaDealsResult> hbaDealsResults = hbaParser.getHbaDealsResultMap();
        updateProgress(0.95, 1); /* this will update the progress bar */
        updateMessage(String.format("Loaded HBA-DEALS results with %d observed genes.", hbaDealsResults.size()));
        this.thresholder = new HbaDealsThresholder(hbaDealsResults);
        updateProgress(1, 1); /* this will update the progress bar */
        updateMessage(String.format("Loaded transcriptToGoMap with %d elements", transcriptToGoMap.size()));
        LOGGER.info(String.format("Loaded transcriptToGoMap with %d elements", transcriptToGoMap.size()));
        updateMessage("Finished loading data for isopret analysis.");

        return 0;
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
                //System.out.println(transcriptAcc.getAccessionString() +": " + geneAcc.getAccessionString());
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

    public Map<AccessionNumber, HgncItem> getHgncMap() {
        return hgncMap;
    }

    public Map<TermId, Set<TermId>> getTranscriptToGoMap() {
        return transcriptToGoMap;
    }

    public InterproMapper getInterproMapper() {
        return interproMapper;
    }

    public HbaDealsThresholder getThresholder() {
        return thresholder;
    }

    public List<String> getErrors() {
        return errors;
    }

    public Map<String, List<Transcript>> getGeneSymbolToTranscriptMap() {
        return geneSymbolToTranscriptMap;
    }

    public AssociationContainer getTranscriptContainer() {
        return transcriptContainer;
    }

    public AssociationContainer getGeneContainer() {
        return geneContainer;
    }
}
