package org.jax.isopret.core.go;


import org.monarchinitiative.phenol.analysis.AssociationContainer;
import org.monarchinitiative.phenol.analysis.DirectAndIndirectTermAnnotations;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class IsopretTranscriptAssociationContainer implements AssociationContainer<TermId>  {
    private final static Logger LOGGER = LoggerFactory.getLogger(IsopretTranscriptAssociationContainer.class);


    /**
     * Key -- TermId for a gene. Value: {@link IsopretTranscriptAnnotations} object with GO annotations for the transcript.
     */
    private final Map<TermId, IsopretTranscriptAnnotations> isoform2associationMap;
    /** Gene Ontology object. */
    private final Ontology ontology;

    public IsopretTranscriptAssociationContainer(Ontology ontology,
                                                 Map<TermId, Set<TermId>> transcriptIdToGoTermsMap){
        this.ontology = ontology;
        Map<TermId, IsopretTranscriptAnnotations> assocMap = new HashMap<>();
        for (var entry : transcriptIdToGoTermsMap.entrySet()) {
            var transcriptId = entry.getKey();
            for (var goId: entry.getValue())  {
                IsopretTermAnnotation termAnnot = new IsopretTermAnnotation(transcriptId, goId);
                assocMap.putIfAbsent(transcriptId, new IsopretTranscriptAnnotations(goId));
                assocMap.get(transcriptId).addAnnotation(termAnnot);
            }
        }
        LOGGER.info("Isopret association container");
        LOGGER.info("Assoc map with {} entries", assocMap.size());
        int c = 0;
        for (var e : assocMap.entrySet()) {
            LOGGER.info("{}) {} -> {}", ++c, e.getKey(), e.getValue());
            if (c>5) break;
        }
        this.isoform2associationMap = Map.copyOf(assocMap);
    }

    @Override
    public Map<TermId, List<TermId>> getOntologyTermToDomainItemsMap() {
        Map<TermId, List<TermId>> mp = new HashMap<>();
        for (Map.Entry<TermId, IsopretTranscriptAnnotations> entry : isoform2associationMap.entrySet()) {
            TermId gene = entry.getKey();
            mp.putIfAbsent(gene, new ArrayList<>());
            for (TermId ontologyTermId : entry.getValue().getAnnotatingTermIds()) {
                mp.get(ontologyTermId).add(gene);
            }
        }
        return mp;
    }

    @Override
    public Map<TermId, DirectAndIndirectTermAnnotations> getAssociationMap(Set<TermId> annotatedItemTermIds) {
        Map<TermId, DirectAndIndirectTermAnnotations> annotationMap = new HashMap<>();
        for (TermId domainTermId : annotatedItemTermIds) {
            IsopretTranscriptAnnotations annots = this.isoform2associationMap.get(domainTermId);
            List<TermId> annotatingIds = annots.getAnnotatingTermIds();
            DirectAndIndirectTermAnnotations dai = new DirectAndIndirectTermAnnotations(new HashSet<>(annotatingIds), ontology);
            annotationMap.put(domainTermId, dai);
        }
        return annotationMap;
    }


    @Override
    public Set<TermId> getAllAnnotatedGenes() {
        return isoform2associationMap.keySet();
    }

    @Override
    public int getAnnotatingTermCount() {
        Set<TermId> tidset = this.isoform2associationMap.values()
                .stream()
                .flatMap(iga -> iga.getAnnotatingTermIds().stream())
                .collect(Collectors.toSet());
        return tidset.size();
    }
}
