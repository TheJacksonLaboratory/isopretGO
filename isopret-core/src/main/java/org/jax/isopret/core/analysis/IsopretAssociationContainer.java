package org.jax.isopret.core.analysis;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import org.jax.isopret.core.transcript.AccessionNumber;
import org.monarchinitiative.phenol.analysis.AssociationContainer;
import org.monarchinitiative.phenol.analysis.DirectAndIndirectTermAnnotations;
import org.monarchinitiative.phenol.analysis.ItemAssociations;
import org.monarchinitiative.phenol.base.PhenolException;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.Term;
import org.monarchinitiative.phenol.ontology.data.TermAnnotation;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static org.monarchinitiative.phenol.ontology.algo.OntologyAlgorithm.getAncestorTerms;

public class IsopretAssociationContainer implements AssociationContainer  {
    private final static Logger LOGGER = LoggerFactory.getLogger(IsopretAssociationContainer.class);



    /**
     * Key -- TermId for a gene. Value: {@link ItemAssociations} object with GO annotations for the gene.
     */
    private final Map<TermId, ItemAssociations> gene2associationMap;
    /**
     * The total number of GO (or HP, MP, etc) terms that are annotating the items in this container.
     * This variable is initialzed only if needed. The getter first checks if it is null, and if so
     * calculates the required count.
     */
    private final int annotatingTermCount;
    /** Gene Ontology object. */
    private final Ontology ontology;


    private final Multimap<TermId, TermId> termToItemMultiMap;

    public IsopretAssociationContainer(Ontology ontology,
                                       Map<TermId, Set<TermId>> transcriptIdToGoTermsMap){
        this.ontology = ontology;
        this.termToItemMultiMap =  ArrayListMultimap.create();
        Map<TermId, ItemAssociations> assocMap = new HashMap<>();
        for (var entry : transcriptIdToGoTermsMap.entrySet()) {
            var transcriptId = entry.getKey();
            for (var goId: entry.getValue())  {
                TermAnnotation termAnnot = new IsopretTermAnnotation(transcriptId, goId);
                assocMap.putIfAbsent(transcriptId, new ItemAssociations(goId));
                assocMap.get(transcriptId).add(termAnnot);
            }
        }
        LOGGER.info("Isopret association container");
        LOGGER.info("Assoc map with {} entries", assocMap.size());
        int c = 0;
        for (var e : assocMap.entrySet()) {
            LOGGER.info("{}) {} -> {}", ++c, e.getKey(), e.getValue());
            if (c>5) break;
        }
        this.gene2associationMap = ImmutableMap.copyOf(assocMap);
        this.annotatingTermCount = gene2associationMap.values()
                .stream()
                .map(ItemAssociations::getAssociations)
                .mapToInt(List::size)
                .sum();
        LOGGER.info("annotatingTermCount {}", annotatingTermCount);
    }


    @Override
    public int getOntologyTermCount() {
        return this.annotatingTermCount;
    }

    @Override
    public Multimap<TermId, TermId> getTermToItemMultimap() {
        Multimap<TermId, TermId> mp = ArrayListMultimap.create();
        for (Map.Entry<TermId, ItemAssociations> entry : gene2associationMap.entrySet()) {
            TermId gene = entry.getKey();
            for (TermId ontologyTermId : entry.getValue().getAssociations()) {
                mp.put(ontologyTermId, gene);
            }
        }
        return mp;
    }

    @Override
    public ItemAssociations get(TermId termId) throws PhenolException {
        if (!this.gene2associationMap.containsKey(termId)) {
            throw new PhenolException("Could not find annotations for " + termId.getValue());
        } else {
            return this.gene2associationMap.get(termId);
        }
    }

    @Override
    public Map<TermId, DirectAndIndirectTermAnnotations> getAssociationMap(Set<TermId> annotatedItemTermIds) {
        return getAssociationMap(annotatedItemTermIds, false);
    }

    public Map<TermId, DirectAndIndirectTermAnnotations> getAssociationMap(Set<TermId> annotatedItemTermIds,
                                                                           boolean verbose) {
        Map<TermId, DirectAndIndirectTermAnnotations> annotationMap = new HashMap<>();
        int not_found = 0;
        for (TermId domainTermId : annotatedItemTermIds) {
            try {
                ItemAssociations assocs = get(domainTermId);
                for (TermAnnotation termAnnotation : assocs) {
                    /* At first add the direct counts and remember the terms */
                    TermId ontologyTermId = termAnnotation.getTermId();
                    // check if the term is in the ontology (sometimes, obsoletes are used in the bla32 files)
                    Term term = this.ontology.getTermMap().get(ontologyTermId);
                    if (term == null) {
                        not_found++;
                        if (verbose) {
                            System.err.println("[WARNING(phenol:AssociationContainer)] Unable to retrieve term "
                                    + ontologyTermId.getValue() + ", omitting.");
                        }
                        continue;
                    }
                    // replace an alt_id with the primary id.
                    // if we already have the primary id, nothing is changed.
                    TermId primaryGoId = term.getId();
                    annotationMap.putIfAbsent(primaryGoId, new DirectAndIndirectTermAnnotations());
                    DirectAndIndirectTermAnnotations termAnnots = annotationMap.get(primaryGoId);
                    termAnnots.addGeneAnnotationDirect(domainTermId);
                    // In addition to the direct annotation, the gene is also indirectly annotated to all of the
                    // GO Term's ancestors
                    Set<TermId> ancs = getAncestorTerms(ontology, primaryGoId, true);
                    for (TermId ancestor : ancs) {
                        annotationMap.putIfAbsent(ancestor, new DirectAndIndirectTermAnnotations());
                        DirectAndIndirectTermAnnotations termAncAnnots = annotationMap.get(ancestor);
                        termAncAnnots.addGeneAnnotationTotal(domainTermId);
                    }
                }
            } catch (PhenolException e) {
                System.err.println("[ERROR (StudySet.java)] " + e.getMessage());
            }
        }
        if (not_found > 0) {
            System.err.printf("[WARNING (AssociationContainer)] Cound not find annotations for %d ontology term ids" +
                    " (are versions of the GAF and obo file compatible?).\n", not_found);
        }
        return annotationMap;
    }


    @Override
    public Set<TermId> getAllAnnotatedGenes() {
        return gene2associationMap.keySet();
    }
}
