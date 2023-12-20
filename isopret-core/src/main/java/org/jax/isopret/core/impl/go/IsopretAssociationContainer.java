package org.jax.isopret.core.impl.go;

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

    public Map<TermId, IsopretAnnotations> getAssociationMap() {
        return associationMap;
    }

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

    /**
     * Get all gene ids (or transcript ids) that are annotated to a given GO term
     * (including descendents)
     * @param goTermId GO term of interest
     * @return all domain items that are annotated to this GO term
     */
    // TODO use new  Set<T> getDomainItemsAnnotatedByOntologyTerm(TermId tid)
    //  in interface AssociationContainer<T> after PR merged to only use Interface in client code
    public Set<TermId> getDomainItemsAnnotatedByGoTerm(TermId goTermId) {
        Set<TermId> domainItemSet = new HashSet<>();
        Set<TermId> descendentSet = OntologyAlgorithm.getDescendents(this.ontology, goTermId);
        for (Map.Entry<TermId, IsopretAnnotations> entry : associationMap.entrySet()) {
            TermId gene = entry.getKey();
            for (TermId ontologyTermId : entry.getValue().getAnnotatingTermIds()) {
                if (descendentSet.contains(ontologyTermId) || ontologyTermId.equals(goTermId)) {
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
                Optional<Term> termOpt = ontology.termForTermId(ontologyTermId);
                if (termOpt.isEmpty()) {
                    ontology_term_not_found++;
                    LOGGER.warn("Unable to retrieve ontology term {} (omitted).", ontologyTermId.getValue());
                    continue;
                }
                ontologyTermId = this.ontology.getPrimaryTermId(ontologyTermId);
                directAnnotationMap.computeIfAbsent(domainTermId, k -> new HashSet<>()).add(ontologyTermId);
            }
        }
        if (domain_termId_not_found > 0) {
            // expected behavior
            LOGGER.trace("Could not find {} domain item term ids.", domain_termId_not_found);
        }
        if (ontology_term_not_found > 0) {
            // expected behavior
            LOGGER.trace("Could not find {} ontology term ids (are go.json/versions in synch?).", ontology_term_not_found);
        }
        Map<TermId, DirectAndIndirectTermAnnotations> annotationMap = new HashMap<>();
        for (Map.Entry<TermId, Set<TermId>> entry : directAnnotationMap.entrySet()) {
            TermId domainItemTermId = entry.getKey();
            for (TermId ontologyId : entry.getValue()) {
                annotationMap.putIfAbsent(ontologyId, new DirectAndIndirectTermAnnotations(ontologyId));
                annotationMap.get(ontologyId).addDirectAnnotatedItem(domainItemTermId);
                // In addition to the direct annotation, the gene is also indirectly annotated
                // to all the GO Term's ancestors
                //Set<TermId> ancs = OntologyAlgorithm.getAncestorTerms(ontology,  ontology.getRootTermId(), ontologyId, false);
                Iterable<TermId> ancs =  ontology.graph().getAncestors(ontologyId, false);
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

    /**
     * TODO ADD TEST
     * @param termId a Gene Ontology id
     * @return set of genes/transcripts annotation by this GO term
     */
    @Override
    public Set<TermId> getDomainItemsAnnotatedByOntologyTerm(TermId termId) {
        Set<TermId> annotatedItems = new HashSet<>();
        for (var e : associationMap.entrySet()) {
            if (e.getValue().containsAnnotation(termId)) {
                annotatedItems.add(e.getKey());
            }
        }
        return annotatedItems;
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
