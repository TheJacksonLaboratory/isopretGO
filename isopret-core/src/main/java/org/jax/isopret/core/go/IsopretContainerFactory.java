package org.jax.isopret.core.go;

import org.monarchinitiative.phenol.analysis.AssociationContainer;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermAnnotation;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This class creates AssociationContainers for genes and isoforms.
 */
public class IsopretContainerFactory {
    private final static Logger LOGGER = LoggerFactory.getLogger(IsopretContainerFactory.class);

    /**
     * Key -- TermId for a gene. Value: {@link IsopretAnnotations} object with GO annotations for the gene.
     */
    private final Map<TermId, Set<TermId>> gene2associationMap;
    private final Map<TermId, Set<TermId>> transcript2associationMap;
    /** Gene Ontology object. */
    private final Ontology ontology;


    public IsopretContainerFactory(Ontology ontology,
                                           Map<TermId, Set<TermId>> transcriptIdToGoTermsMap,
                                           Map<TermId, Set<TermId>> geneIdToGoTermsMap){
        this.ontology = ontology;
        this.transcript2associationMap = transcriptIdToGoTermsMap;
        this.gene2associationMap = geneIdToGoTermsMap;
    }




    public AssociationContainer<TermId> transcriptContainer() {
        Map<TermId, IsopretAnnotations> assocMap = new HashMap<>();
        for (var entry : transcript2associationMap.entrySet()) {
            var transcriptId = entry.getKey();
            // Remove any GO terms that are not in the Ontology -- this could
            // represent a version error
            Set<TermId> annotatingGoTerms = entry.getValue()
                    .stream()
                    .filter(ontology::containsTerm)
                    .collect(Collectors.toSet());
            List<TermAnnotation> termAnnots = new ArrayList<>();
            for (TermId goId : annotatingGoTerms) {
                termAnnots.add(new IsopretTermAnnotation(transcriptId, goId));
            }
            IsopretAnnotations isopretAnnotations = new IsopretAnnotations(transcriptId, termAnnots);
            assocMap.put(transcriptId, isopretAnnotations);
        }
        LOGGER.info("Isopret association container");
        LOGGER.info("Isopret transcript association containiner - map with {} entries", assocMap.size());
        return new IsopretAssociationContainer(ontology, assocMap);
    }


    public AssociationContainer<TermId> geneContainer() {
        Map<TermId, IsopretAnnotations> assocMap = new HashMap<>();
        for (var entry : gene2associationMap.entrySet()) {
            var geneId = entry.getKey();
            // Remove any GO terms that are not in the Ontology -- this could
            // represent a version error
            Set<TermId> annotatingGoTerms = entry.getValue()
                    .stream()
                    .filter(ontology::containsTerm)
                    .collect(Collectors.toSet());
            List<TermAnnotation> termAnnots = new ArrayList<>();
            for (TermId goId : annotatingGoTerms) {
                termAnnots.add(new IsopretTermAnnotation(geneId, goId));
            }
            IsopretAnnotations isopretAnnotations = new IsopretAnnotations(geneId, termAnnots);
            assocMap.put(geneId, isopretAnnotations);
        }
        LOGGER.info("Isopret association container");
        LOGGER.info("Isopret transcript association containiner - map with {} entries", assocMap.size());
        return new IsopretAssociationContainer(ontology, assocMap);

    }


}
