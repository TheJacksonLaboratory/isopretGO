package org.jax.isopret.gui.service.model;

import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.monarchinitiative.phenol.analysis.stats.GoTerm2PValAndCounts;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * This class is used to compare two sets of significant GO terms (DGE vs DAS).
 * It is intended to be used as the data for a paired bar chart in the GUI
 * @author Peter N Robinson
 */
public class GoComparison {

    private final static double ALPHA = 0.05;

    private final List<GoCompTerm> goCompTermList;

    public GoComparison(List<GoTerm2PValAndCounts> dge,
                        List<GoTerm2PValAndCounts> das,
                        Ontology ontology) {
        // first collect all significant terms from DGE or DAS
        Set<TermId> significantGoTerms = dge.stream()
                .filter(t -> t.getAdjustedPValue() <= ALPHA)
                .map(GoTerm2PValAndCounts::getGoTermId)
                .collect(Collectors.toSet());
        significantGoTerms.addAll(das.stream()
                .filter(t -> t.getAdjustedPValue() <= ALPHA)
                .map(GoTerm2PValAndCounts::getGoTermId)
                .collect(Collectors.toSet()));
        // collect maps for these terms in DGE and DAS
        Map<TermId, Double> sigDgeMap = dge.stream()
                .filter(g -> significantGoTerms.contains(g.getGoTermId()))
                .collect(Collectors.toMap(GoTerm2PValAndCounts::getGoTermId,
                        GoTerm2PValAndCounts::getAdjustedPValue));
        Map<TermId, Double> sigDasMap = das.stream()
                .filter(g -> significantGoTerms.contains(g.getGoTermId()))
                .collect(Collectors.toMap(GoTerm2PValAndCounts::getGoTermId,
                        GoTerm2PValAndCounts::getAdjustedPValue));
        List<GoCompTerm> goCompTerms = new ArrayList<>();
        for (TermId goId : significantGoTerms) {
            double dgeLog10P = -1*Math.log10(sigDgeMap.getOrDefault(goId, 1d));
            double dasLog10P = -1*Math.log10(sigDasMap.getOrDefault(goId, 1d));
            Optional<String> opt = ontology.getTermLabel(goId);
            String label = opt.orElse("n/a"); // we should never get n/a
            GoCompTerm compTerm = new GoCompTerm(goId, label, dgeLog10P, dasLog10P);
            goCompTerms.add(compTerm);
        }
        Collections.sort(goCompTerms);
        this.goCompTermList = List.copyOf(goCompTerms);//make immutable
    }

    public List<GoCompTerm> getGoCompTermList() {
        return goCompTermList;
    }

    private Predicate<GoCompTerm> dgePredominent = g -> g.getDge() >= g.getDas();
    private Predicate<GoCompTerm> dasPredominent = g -> g.getDas() > g.getDge();


    public List<GoCompTerm> getDgePredominentGoCompTerms() {
        return goCompTermList.stream()
                .filter(dgePredominent)
                .collect(Collectors.toList());
    }

    public List<GoCompTerm> getDasPredominentGoCompTerms() {
        return goCompTermList.stream()
                .filter(dasPredominent)
                .collect(Collectors.toList());
    }



}
