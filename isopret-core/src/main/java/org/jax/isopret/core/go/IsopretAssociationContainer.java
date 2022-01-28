package org.jax.isopret.core.go;

import org.monarchinitiative.phenol.analysis.AssociationContainer;
import org.monarchinitiative.phenol.analysis.DirectAndIndirectTermAnnotations;
import org.monarchinitiative.phenol.ontology.algo.OntologyAlgorithm;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.Term;
import org.monarchinitiative.phenol.ontology.data.TermAnnotation;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This class extends a class from phenol and contains the information
 * that we need to do GO analysis. isopret will initialize this class once for
 * transcripts and once for genes. The key object in the class is the
 * {@link #associationMap}. The keys for this map are the gene or transcript ids
 * (expressed as TermIds), and the values are {@link IsopretAnnotations} objects
 * that represent the annotated genes.
 */
public class IsopretAssociationContainer implements AssociationContainer<TermId> {

    private final static Logger LOGGER = LoggerFactory.getLogger(IsopretAssociationContainer.class);

    /**
     * Key -- TermId for a gene. Value: {@link IsopretAnnotations} object with GO annotations for the gene.
     */
    private final Map<TermId, IsopretAnnotations> associationMap;
    /** Gene Ontology object. */
    private final Ontology ontology;

    private final int n_annotations;

    IsopretAssociationContainer(Ontology ontology,
                                Map<TermId, IsopretAnnotations> assocMap){
        this.ontology = ontology;
        this.associationMap = assocMap;
        n_annotations = assocMap.values()
                .stream()
                .map(IsopretAnnotations::getAnnotationCount)
                .reduce(0, Integer::sum);

    }

    /**
     *
     * @return map with key -- a GO term id, value -- list of annotated genes.
     */
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

    public Set<TermId> getDomainItemsAnnotatedByGoTerm(TermId goTermId) {
        Set<TermId> domainItemSet = new HashSet<>();
        for (Map.Entry<TermId, IsopretAnnotations> entry : associationMap.entrySet()) {
            TermId gene = entry.getKey();
            for (TermId ontologyTermId : entry.getValue().getAnnotatingTermIds()) {
                if (goTermId.equals(ontologyTermId)) {
                    domainItemSet.add(gene);
                }
            }
        }
        return domainItemSet;
    }



    @Override
    public Map<TermId, DirectAndIndirectTermAnnotations> getAssociationMap(Set<TermId> annotatedItemTermIds) {
        // 1. Get all of the direct GO annotations to the genes. Key: domain item; value: annotating Ontlogy terms
        Map<TermId, Set<TermId>> directAnnotationMap = new HashMap<>();
        int domain_termId_not_found = 0;
        int ontology_term_not_found = 0;
        for (TermId domainTermId : annotatedItemTermIds) {
            if (!this.associationMap.containsKey(domainTermId)) {
                domain_termId_not_found++;
                continue;
            }
            IsopretAnnotations assocs = this.associationMap.get(domainTermId);
            for (TermAnnotation termAnnotation : assocs.getAnnotations()) {
                IsopretTermAnnotation ita = (IsopretTermAnnotation) termAnnotation;

                /* In this step add the direct annotations only */
                TermId ontologyTermId = ita.getTermId();
                // check if the term is in the ontology (sometimes, obsoletes are used in the bla32 files)
                Term term = this.ontology.getTermMap().get(ontologyTermId);
                if (term == null) {
                    ontology_term_not_found++;
                    LOGGER.warn("Unable to retrieve ontology term {} (omitted).", ontologyTermId.getValue());
                    continue;
                }
                // if necessary, replace with the latest primary term id
                ontologyTermId = this.ontology.getPrimaryTermId(ontologyTermId);
                directAnnotationMap.computeIfAbsent(domainTermId, k -> new HashSet<>()).add(ontologyTermId);
            }
        }
        if (domain_termId_not_found > 0) {
            LOGGER.warn("Could not find {} domain item term ids.", domain_termId_not_found);
        }
        if (ontology_term_not_found > 0) {
            LOGGER.warn("Could not find {} ontology term ids (are go.json/versions in synch?).", ontology_term_not_found);
        }
        Map<TermId, DirectAndIndirectTermAnnotations> annotationMap = new HashMap<>();
        for (Map.Entry<TermId, Set<TermId>> entry : directAnnotationMap.entrySet()) {
            TermId domainItemTermId = entry.getKey();
            for (TermId ontologyId : entry.getValue()) {
                annotationMap.putIfAbsent(ontologyId, new DirectAndIndirectTermAnnotations(ontologyId));
                annotationMap.get(ontologyId).addDirectAnnotatedItem(domainItemTermId);
                // In addition to the direct annotation, the gene is also indirectly annotated
                // to all of the GO Term's ancestors
                Set<TermId> ancs = OntologyAlgorithm.getAncestorTerms(ontology, ontologyId, false);
                for (TermId ancestor : ancs) {
                    annotationMap.putIfAbsent(ancestor, new DirectAndIndirectTermAnnotations(ancestor));
                    annotationMap.get(ancestor).addIndirectAnnotatedItem(domainItemTermId);
                }
            }
        }
        return Map.copyOf(annotationMap); //make immutable
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

    @Override
    public int getTotalAnnotationCount() {
        return this.n_annotations;
    }

    @Override
    public int getAnnotatedDomainItemCount() {
        return associationMap.size();
    }
}
