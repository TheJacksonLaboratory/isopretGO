package org.jax.isopret.core.impl.go;

import org.monarchinitiative.phenol.analysis.AssociationContainer;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.Term;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * This is a conveniece class that adds gene symbols to the GO results.
 * The challenge is that we want to show those GO terms that are significantly enriched togehter with the
 * genes they annotate. Typically, this will be a small subset of all of the GO terms that annotate a gene.
 * Therefore, this class takes a set of genes that show either DGE or DAS as well as a set of overrepresented
 * GO terms, and returns a Map with key (gene) and value (set of associated overrepresented GO terms).
 */
public class HbaGene2Go {
    final Logger LOGGER = LoggerFactory.getLogger(HbaGene2Go.class);

    private final Ontology ontology;
    private final AssociationContainer<TermId> geneContainer;
    private final AssociationContainer<TermId> transcriptContainer;

    /**
     * @param ontology Gene Ontology object
     * @param geneContainer link between the genes and GO annotations
     * @param transcriptContainer  link between the transcripts and GO annotations
     */
    public HbaGene2Go(Ontology ontology,AssociationContainer<TermId> geneContainer,
                      AssociationContainer<TermId> transcriptContainer){
        this.ontology = ontology;
        this.geneContainer = geneContainer;
        this.transcriptContainer = transcriptContainer;
    }

    /**
     * @param overrepresentedGoTermIdSet All GO terms that are overrepresented in either DGE or DAS
     * @param significantGeneSymbols All genes that show DGE

     * @return Map between the genes (key) and the overrepresented GO Terms.
     */
    public Map<TermId, Set<Term>> getSignificantGeneToOverrepresentedGoMap(Set<TermId> overrepresentedGoTermIdSet,
                                                                    Set<TermId> significantGeneSymbols) {
        Map<TermId, Set<Term>> symbolToGoTermResults = new HashMap<>();
        Map<TermId, List<TermId>> goMap = geneContainer.getOntologyTermToDomainItemsMap();
        for (TermId overrepresentedGoTermId : overrepresentedGoTermIdSet) {
            for (TermId geneTermId : goMap.getOrDefault(overrepresentedGoTermId, new ArrayList<>())) {
                if (significantGeneSymbols.contains(geneTermId)) {
                   if (ontology.containsTerm(overrepresentedGoTermId) ) {
                       Term term = ontology.getTermMap().get(overrepresentedGoTermId);
                       symbolToGoTermResults.putIfAbsent(geneTermId, new HashSet<>());
                       symbolToGoTermResults.get(geneTermId).add(term);
                   } else {
                       LOGGER.error("Could not find term for {}", overrepresentedGoTermId.getValue());
                   }
                }
            }
        }
        return symbolToGoTermResults;
    }

    /**
     * @param overrepresentedGoTermIdSet All GO terms that are overrepresented in either DGE or DAS
     * @param significantIsoforms All isoforms that show DAS

     * @return Map between the genes (key) and the overrepresented GO Terms.
     */
    public Map<TermId, Set<Term>> getSignificantIsoformToOverrepresentedGoMap(Set<TermId> overrepresentedGoTermIdSet,
                                                                           Set<TermId> significantIsoforms) {
        Map<TermId, Set<Term>> isoformToGoTermResults = new HashMap<>();
        Map<TermId, List<TermId>> goMap = transcriptContainer.getOntologyTermToDomainItemsMap();
        for (TermId overrepresentedGoTermId : overrepresentedGoTermIdSet) {
            for (TermId geneTermId : goMap.getOrDefault(overrepresentedGoTermId, new ArrayList<>())) {
                if (significantIsoforms.contains(geneTermId)) {
                    if (ontology.containsTerm(overrepresentedGoTermId) ) {
                        Term term = ontology.getTermMap().get(overrepresentedGoTermId);
                        isoformToGoTermResults.putIfAbsent(geneTermId, new HashSet<>());
                        isoformToGoTermResults.get(geneTermId).add(term);
                    } else {
                        LOGGER.error("Could not find term for {}", overrepresentedGoTermId.getValue());
                    }
                }
            }
        }
        return isoformToGoTermResults;
    }

}
