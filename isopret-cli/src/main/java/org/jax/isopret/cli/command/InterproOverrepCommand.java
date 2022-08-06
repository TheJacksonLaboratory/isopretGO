package org.jax.isopret.cli.command;

import org.jax.isopret.core.IsopretProvider;
import org.jax.isopret.core.analysis.InterproFisherExact;
import org.jax.isopret.core.analysis.InterproOverrepResult;
import org.jax.isopret.core.except.IsopretException;
import org.jax.isopret.core.except.IsopretRuntimeException;
import org.jax.isopret.core.impl.go.IsopretAssociationContainer;
import org.jax.isopret.core.impl.go.IsopretContainerFactory;
import org.jax.isopret.core.impl.hbadeals.HbaDealsIsoformSpecificThresholder;
import org.jax.isopret.core.impl.hbadeals.HbaDealsParser;
import org.jax.isopret.core.impl.hbadeals.HbaDealsResult;
import org.jax.isopret.core.impl.interpro.DisplayInterproAnnotation;
import org.jax.isopret.core.impl.interpro.InterproMapper;
import org.jax.isopret.model.GeneModel;
import org.jax.isopret.model.AccessionNumber;
import org.jax.isopret.model.AnnotatedGene;
import org.jax.isopret.model.GeneSymbolAccession;
import org.jax.isopret.model.Transcript;
import org.jax.isopret.visualization.InterproOverrepVisualizer;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
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

    private IsopretAssociationContainer transcriptContainer = null;
    private IsopretAssociationContainer geneContainer = null;
    /** Key ensembl transcript id; values: annotating go terms .*/
    private Map<TermId, Set<TermId>> transcriptToGoMap = Map.of();

    private Map<GeneSymbolAccession, List<Transcript>> geneSymbolAccessionToTranscriptMap = Map.of();

    private Double splicingPepThreshold = null;

    private InterproMapper interproMapper = null;

    private IsopretProvider provider = null;

    @Override
    public Integer call() throws IsopretException {
        provider = IsopretProvider.provider(Paths.get(this.downloadDirectory));
        interproMapper = provider.interproMapper();
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




    private HbaDealsIsoformSpecificThresholder getThresholder(String hbaDealsFilePath) throws IsopretException {
        geneSymbolAccessionToTranscriptMap = provider.geneSymbolToTranscriptListMap();
        Map<AccessionNumber, GeneModel> hgncMap  = provider.ensemblGeneModelMap();
        Ontology ontology = provider.geneOntology();
        HbaDealsParser hbaParser = new HbaDealsParser(hbaDealsFilePath, hgncMap);
        Map<AccessionNumber, HbaDealsResult> hbaDealsResults = hbaParser.getEnsgAcc2hbaDealsMap();
        this.transcriptToGoMap = provider.transcriptIdToGoTermsMap();
        Map<TermId, TermId> transcriptToGeneIdMap = provider.transcriptToGeneIdMap();
        Map<TermId, Set<TermId>> gene2GoMap = provider.gene2GoMap();
        IsopretContainerFactory isoContainerFac = new IsopretContainerFactory(ontology, transcriptToGoMap, gene2GoMap);
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

    public List<AnnotatedGene> getAnnotatedGeneList() throws IsopretException{
        int notfound = 0;
        HbaDealsIsoformSpecificThresholder thresholder = getThresholder(hbadealsFile);
        this.splicingPepThreshold = thresholder.getSplicingPepThreshold();
        this.geneSymbolAccessionToTranscriptMap = provider.geneSymbolToTranscriptListMap();
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
        InterproMapper interproMapper = provider.interproMapper();
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



}
