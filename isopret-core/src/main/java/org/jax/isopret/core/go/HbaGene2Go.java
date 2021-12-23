package org.jax.isopret.core.go;

import org.monarchinitiative.phenol.analysis.AssociationContainer;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermAnnotation;
import org.monarchinitiative.phenol.ontology.data.TermId;

import java.util.*;

/**
 * This is a conveniece class that adds gene symbols to the GO results.
 */
public class HbaGene2Go {

    private final Ontology ontology;

    public HbaGene2Go(Ontology ontology){
        this.ontology = ontology;
    }

    public Map<String, Set<GoTermIdPlusLabel>> getEnrichedSymbolToEnrichedGoMap(Set<TermId> einrichedGoTermIdSet,
                                                                                Set<String> symbols,
                                                                                IsopretGeneAssociationContainer container) {
        Map<String, Set<GoTermIdPlusLabel>> symbolToGoTermResults = new HashMap<>();
        //List<TermAnnotation> rawAnnots = this.associationContainer.getRawAssociations();
        for (TermAnnotation a : rawAnnots) {
            String symbol = a.getDbObjectSymbol();
            if (symbols.contains(symbol)) {
                symbolToGoTermResults.putIfAbsent(symbol, new HashSet<>());
                TermId goId = a.getGoId();
                if (einrichedGoTermIdSet.contains(goId)) {
                    Optional<String> labelOpt = ontology.getTermLabel(goId);
                    labelOpt.ifPresent(s -> symbolToGoTermResults.get(symbol).add(new GoTermIdPlusLabel(goId, s)));
                }
            }
        }
        return symbolToGoTermResults;
    }

}
