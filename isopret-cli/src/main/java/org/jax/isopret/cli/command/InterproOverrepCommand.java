package org.jax.isopret.cli.command;

import org.jax.isopret.core.analysis.InterproFisherExact;
import org.jax.isopret.core.analysis.InterproOverrepResult;
import org.jax.isopret.core.except.IsopretException;
import org.jax.isopret.core.except.IsopretRuntimeException;
import org.jax.isopret.core.go.IsopretAssociationContainer;
import org.jax.isopret.core.go.IsopretContainerFactory;
import org.jax.isopret.core.hbadeals.HbaDealsIsoformSpecificThresholder;
import org.jax.isopret.core.hbadeals.HbaDealsParser;
import org.jax.isopret.core.hbadeals.HbaDealsResult;
import org.jax.isopret.core.hgnc.HgncParser;
import org.jax.isopret.model.GeneModel;
import org.jax.isopret.core.interpro.DisplayInterproAnnotation;
import org.jax.isopret.core.interpro.InterproMapper;
import org.jax.isopret.core.io.TranscriptFunctionFileParser;
import org.jax.isopret.model.AccessionNumber;
import org.jax.isopret.model.AnnotatedGene;
import org.jax.isopret.model.GeneSymbolAccession;
import org.jax.isopret.model.Transcript;
import org.jax.isopret.core.visualization.InterproOverrepVisualizer;
import org.monarchinitiative.phenol.io.OntologyLoader;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "interpro",
        mixinStandardHelpOptions = true,
        description = "Interpro Overrepresentation")
public class InterproOverrepCommand extends AbstractIsopretCommand implements Callable<Integer> {
    private static final Logger LOGGER = LoggerFactory.getLogger(InterproOverrepCommand.class);
    @CommandLine.Option(names={"-b","--hbadeals"},
            description ="HBA-DEALS output file" , required = true)
    private String hbadealsFile;
    @CommandLine.Option(names={"--outfile"}, description = "Name of output file to write stats (default: ${DEFAULT-VALUE})")
    private String outfile = "isopret-interpro-overrep.txt";

    private Ontology geneOntology = null;
    private IsopretAssociationContainer transcriptContainer = null;
    private IsopretAssociationContainer geneContainer = null;
    /** Key ensembl transcript id; values: annotating go terms .*/
    private Map<TermId, Set<TermId>> transcriptToGoMap = Map.of();

    private Map<GeneSymbolAccession, List<Transcript>> geneSymbolAccessionToTranscriptMap = Map.of();

    private Double splicingPepThreshold = null;

    private InterproMapper interproMapper = null;

    @Override
    public Integer call() throws IsopretException {
        interproMapper = getInterproMapper();
        LOGGER.info("Got interpro mapper");
        List<AnnotatedGene> annotatedGeneList = getAnnotatedGeneList();
        LOGGER.info("Got {} Annotated genes.", annotatedGeneList.size());
        if (splicingPepThreshold == null) {
            throw new IsopretRuntimeException("Could not calculate splicing PEP threshold (should never happen!)");
        }
        InterproFisherExact ife = new InterproFisherExact(annotatedGeneList, splicingPepThreshold);
        List<InterproOverrepResult> results = ife.calculateInterproOverrepresentation();
        LOGGER.info("Got {} InterproOverrepResults.", results.size());
        InterproOverrepVisualizer visualizer = new InterproOverrepVisualizer(results);
        if (outfile == null) {
            outfile = getDefaultOutfileName("interpro", hbadealsFile);
        }
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(outfile))) {
            bw.write(visualizer.getTsv());
        } catch (IOException e) {
           e.printStackTrace();
        }
        return null;
    }

    private Ontology getGeneOntology() {
        if (this.geneOntology == null) {
            File goJsonFile = new File(downloadDirectory + File.separator + "go.json");
            if (!goJsonFile.isFile()) {
                throw new IsopretRuntimeException("Could not find Gene Ontology JSON file at " + goJsonFile.getAbsolutePath());
            }
            this.geneOntology = OntologyLoader.loadOntology(goJsonFile);
        }
        return this.geneOntology;
    }


    private HbaDealsIsoformSpecificThresholder getThresholder(String hbaDealsFilePath) throws IsopretException {
        File hgncFile = new File(downloadDirectory + File.separator + "hgnc_complete_set.txt");
        geneSymbolAccessionToTranscriptMap = loadJannovarSymbolToTranscriptMap();
        HgncParser hgncParser = new HgncParser(hgncFile, geneSymbolAccessionListMap);
        Map<AccessionNumber, GeneModel> hgncMap  = hgncParser.ensemblMap();
        LOGGER.info("Loaded Ensembl HGNC map with {} genes", hgncMap.size());
        HbaDealsParser hbaParser = new HbaDealsParser(hbaDealsFilePath, hgncMap);
        Map<AccessionNumber, HbaDealsResult> hbaDealsResults = hbaParser.getEnsgAcc2hbaDealsMap();
        Ontology ontology = getGeneOntology();
        TranscriptFunctionFileParser fxnparser = new TranscriptFunctionFileParser(new File(downloadDirectory), ontology);
        this.transcriptToGoMap = fxnparser.getTranscriptIdToGoTermsMap();
        Map<TermId, TermId> transcriptToGeneIdMap = createTranscriptToGeneIdMap(geneSymbolAccessionToTranscriptMap);
        Map<TermId, Set<TermId>> gene2GoMap = fxnparser.getGeneIdToGoTermsMap(transcriptToGeneIdMap);
        IsopretContainerFactory isoContainerFac = new IsopretContainerFactory(geneOntology, transcriptToGoMap, gene2GoMap);
        LOGGER.info("Loaded gene2GoMap with {} entries", gene2GoMap.size());
        transcriptContainer = isoContainerFac.transcriptContainer();
        LOGGER.info("Got transcriptContainer with {} domain items", transcriptContainer.getAnnotatedDomainItemCount());
        geneContainer = isoContainerFac.geneContainer();
        LOGGER.info("Got geneContainer with {} domain items", geneContainer.getAnnotatedDomainItemCount());
        HbaDealsIsoformSpecificThresholder thresholder = new HbaDealsIsoformSpecificThresholder(hbaDealsResults,
                0.05,
                geneContainer,
                transcriptContainer);
        LOGGER.info("Got thresholder with {} DAS isoforms", thresholder.getDasIsoformCount());
        return thresholder;
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

    public List<AnnotatedGene> getAnnotatedGeneList() throws IsopretException{
        int notfound = 0;
        HbaDealsIsoformSpecificThresholder thresholder = getThresholder(hbadealsFile);
        this.splicingPepThreshold = thresholder.getSplicingPepThreshold();
        this.geneSymbolAccessionToTranscriptMap = loadJannovarSymbolToTranscriptMap();
        List<AnnotatedGene> annotatedGenes = new ArrayList<>();
        // sort the raw results according to minimum p-values
        List<HbaDealsResult> results = thresholder.getRawResults().values()
                .stream()
                .sorted()
                .toList();
        LOGGER.info("Got {} raw results from thresholder", results.size());
        int c = 0;
        double splicingThreshold = thresholder.getSplicingPepThreshold();
        double expressionThreshold = thresholder.getExpressionPepThreshold();
        InterproMapper interproMapper = getInterproMapper();
        for (HbaDealsResult result : results) {
            c++;
            if (c % 100==0) {
                LOGGER.info("results {} not found {}", c, notfound);
            }
            if (! this.geneSymbolAccessionToTranscriptMap.containsKey(result.getGeneSymbolAccession())) {
                notfound++;
                continue;
            }
            List<Transcript> transcripts = this.geneSymbolAccessionToTranscriptMap.get(result.getGeneSymbolAccession());
            Map<AccessionNumber, List<DisplayInterproAnnotation>> transcriptToInterproHitMap =
                    interproMapper.transcriptToInterproHitMap(result.getGeneAccession());
            AnnotatedGene agene = new AnnotatedGene(transcripts,
                    transcriptToInterproHitMap,
                    result,
                    expressionThreshold,
                    splicingThreshold);
            annotatedGenes.add(agene);
        }
        if (notfound > 0) {
            LOGGER.warn("Could not find transcript map for {} genes", notfound);
        }
        return annotatedGenes;
    }


    private InterproMapper getInterproMapper() {
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
        return new InterproMapper(interproDescriptionFile, interproDomainsFile);
    }


}
