package org.jax.isopret.core.go;

import org.monarchinitiative.phenol.analysis.AssociationContainer;
import org.monarchinitiative.phenol.analysis.DirectAndIndirectTermAnnotations;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class IsopretAssociationContainer implements AssociationContainer<TermId> {

    private final static Logger LOGGER = LoggerFactory.getLogger(IsopretAssociationContainer.class);

    /**
     * Key -- TermId for a gene. Value: {@link IsopretAnnotations} object with GO annotations for the gene.
     */
    private final Map<TermId, IsopretAnnotations> associationMap;
    /** Gene Ontology object. */
    private final Ontology ontology;

    IsopretAssociationContainer(Ontology ontology,
                                Map<TermId, IsopretAnnotations> assocMap){
        this.ontology = ontology;
        this.associationMap = assocMap;

    }

    @Override
    public Map<TermId, List<TermId>> getOntologyTermToDomainItemsMap() {
        Map<TermId, List<TermId>> mp = new HashMap<>();
        for (Map.Entry<TermId, IsopretAnnotations> entry : associationMap.entrySet()) {
            TermId gene = entry.getKey();
            for (TermId ontologyTermId : entry.getValue().getAnnotatingTermIds()) {
                mp.putIfAbsent(ontologyTermId, new ArrayList<>());
                mp.get(ontologyTermId).add(gene);
            }
        }
        return mp;
    }

    @Override
    public Map<TermId, DirectAndIndirectTermAnnotations> getAssociationMap(Set<TermId> annotatedItemTermIds) {
        Map<TermId, DirectAndIndirectTermAnnotations> annotationMap = new HashMap<>();
        for (TermId domainTermId : annotatedItemTermIds) {
            IsopretAnnotations assocs = this.associationMap.get(domainTermId);
            if (assocs == null) {
                LOGGER.warn("Could not retrieve assocs for {}.", domainTermId);
                continue;
            }
            List<TermId> annotatingIds = assocs.getAnnotatingTermIds();
            try {
                DirectAndIndirectTermAnnotations dai = new DirectAndIndirectTermAnnotations(new HashSet<>(annotatingIds), ontology);
                annotationMap.put(domainTermId, dai);
            } catch (Exception e) {
                // A no such vertex in graph Exception is possible if the
                // ontology file and the associations are not in synch.
                // We need to catch this to avoid crashs
                LOGGER.warn("Could not construct annotations for {}: {}", domainTermId.getValue(), e.getMessage());
            }
        }
        return annotationMap;
    }

    @Override
    public Set<TermId> getAllAnnotatedGenes() {
        return associationMap.keySet();
    }

    @Override
    public int getAnnotatingTermCount() {
        Set<TermId> tidset = this.associationMap.values()
                .stream()
                .flatMap(iga -> iga.getAnnotatingTermIds().stream())
                .collect(Collectors.toSet());
        return tidset.size();
    }
}
