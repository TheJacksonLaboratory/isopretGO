package org.jax.isopret.core.impl.interpro;

import org.jax.isopret.core.InterproAnalysisResults;
import org.jax.isopret.core.InterproMapper;
import org.jax.isopret.core.IsopretInterpoAnalysisRunner;
import org.jax.isopret.core.IsopretProvider;
import org.jax.isopret.core.analysis.InterproFisherExact;
import org.jax.isopret.core.analysis.InterproOverrepResult;
import org.jax.isopret.core.impl.go.IsopretAssociationContainer;
import org.jax.isopret.core.impl.go.IsopretContainerFactory;
import org.jax.isopret.core.impl.rnaseqdata.IsoformSpecificThresholder;
import org.jax.isopret.core.impl.rnaseqdata.RnaSeqResultsParser;
import org.jax.isopret.except.IsopretRuntimeException;
import org.jax.isopret.model.*;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;

public class IsopretInterpoAnalysisRunnerImpl implements IsopretInterpoAnalysisRunner {
    private final static Logger LOGGER = LoggerFactory.getLogger(IsopretInterpoAnalysisRunnerImpl.class);

    private final List<AnnotatedGene> annotatedGeneList;

    private IsopretAssociationContainer transcriptContainer = null;
    private IsopretAssociationContainer geneContainer = null;
    /** Key ensembl transcript id; values: annotating go terms .*/
    private Map<TermId, Set<TermId>> transcriptToGoMap = Map.of();

    private Map<GeneSymbolAccession, List<Transcript>> geneSymbolAccessionToTranscriptMap = Map.of();

    private Double splicingPepThreshold = null;




    public static IsopretInterpoAnalysisRunner of(String hbadealsFile, IsopretProvider provider) {
        return new IsopretInterpoAnalysisRunnerImpl(hbadealsFile, provider);
    }

    public static IsopretInterpoAnalysisRunner of(List<AnnotatedGene> annotatedGeneList, double splicingPep) {
        return new IsopretInterpoAnalysisRunnerImpl(annotatedGeneList, splicingPep);
    }

    private IsopretInterpoAnalysisRunnerImpl(String hbadealsFile, IsopretProvider provider) {
        this.annotatedGeneList = getAnnotatedGeneList(hbadealsFile, provider);
    }

    private IsopretInterpoAnalysisRunnerImpl(List<AnnotatedGene> annotatedGeneList, double splicingPep) {
        this.annotatedGeneList = annotatedGeneList;
        this.splicingPepThreshold = splicingPep;
    }

    @Override
    public InterproAnalysisResults run() {
        LOGGER.info("Got {} Annotated genes.", annotatedGeneList.size());
        if (splicingPepThreshold == null) {
            throw new IsopretRuntimeException("Could not calculate splicing PEP threshold (should never happen!)");
        }
        InterproFisherExact ife = new InterproFisherExact(annotatedGeneList, splicingPepThreshold);
        List<InterproOverrepResult> results = ife.calculateInterproOverrepresentation();
        Collections.sort(results);
        return new InterproResultsDefault(results);
    }


    private IsoformSpecificThresholder getThresholder(String hbadealsFile, IsopretProvider provider)  {
        geneSymbolAccessionToTranscriptMap = provider.geneSymbolToTranscriptListMap();
        Map<AccessionNumber, GeneModel> hgncMap  = provider.ensemblGeneModelMap();
        Ontology ontology = provider.geneOntology();
        File file = new File(hbadealsFile);
        if (! file.isFile()) {
            throw new IsopretRuntimeException("Could not fine HBA-DEAL file at " + file.getAbsoluteFile());
        }
        Map<AccessionNumber, GeneResult> hbaDealsResults = RnaSeqResultsParser.fromHbaDeals(file, hgncMap);
        this.transcriptToGoMap = provider.transcriptIdToGoTermsMap();
        Map<TermId, TermId> transcriptToGeneIdMap = provider.transcriptToGeneIdMap();
        Map<TermId, Set<TermId>> gene2GoMap = provider.gene2GoMap();
        IsopretContainerFactory isoContainerFac = new IsopretContainerFactory(ontology, transcriptToGoMap, gene2GoMap);
        LOGGER.info("Loaded gene2GoMap with {} entries", gene2GoMap.size());
        transcriptContainer = isoContainerFac.transcriptContainer();
        LOGGER.info("Got transcriptContainer with {} domain items", transcriptContainer.getAnnotatedDomainItemCount());
        geneContainer = isoContainerFac.geneContainer();
        LOGGER.info("Got geneContainer with {} domain items", geneContainer.getAnnotatedDomainItemCount());
        IsoformSpecificThresholder thresholder =  IsoformSpecificThresholder.fromHbaDeals(hbaDealsResults,
                0.05,
                geneContainer,
                transcriptContainer);
        LOGGER.info("Got thresholder with {} DAS isoforms", thresholder.getDasIsoformCount());
        return thresholder;
    }


    private List<AnnotatedGene> getAnnotatedGeneList(String hbadealsFile, IsopretProvider provider) {
        int notfound = 0;
        IsoformSpecificThresholder thresholder = getThresholder(hbadealsFile , provider);
        this.splicingPepThreshold = thresholder.getSplicingPepThreshold();
        this.geneSymbolAccessionToTranscriptMap = provider.geneSymbolToTranscriptListMap();
        List<AnnotatedGene> annotatedGenes = new ArrayList<>();
        // sort the raw results according to minimum p-values
        List<GeneResult> results = thresholder.getRawResults().values()
                .stream()
                .sorted()
                .toList();
        LOGGER.info("Got {} raw results from thresholder", results.size());
        int c = 0;
        double splicingThreshold = thresholder.getSplicingPepThreshold();
        double expressionThreshold = thresholder.getExpressionPepThreshold();
        InterproMapper interproMapper = provider.interproMapper();
        for (GeneResult result : results) {
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

