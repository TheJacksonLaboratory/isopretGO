package org.jax.isopret.core.go;

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


    private IsopretAssociationContainer getContainer(Map<TermId, Set<TermId>> assocmap) {
        Map<TermId, IsopretAnnotations> assocMap = new HashMap<>();
        for (var entry : assocmap.entrySet()) {
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
        LOGGER.info("Isopret association containiner - map with {} entries", assocmap.size());
        return new IsopretAssociationContainer(ontology, assocMap);
    }


    public IsopretAssociationContainer transcriptContainer() {
        LOGGER.info("Constructing transcript association container");
        return getContainer(this.transcript2associationMap);
    }


    public IsopretAssociationContainer geneContainer() {
        LOGGER.info("Constructing gene association container");
        return getContainer(this.gene2associationMap);
    }


}
