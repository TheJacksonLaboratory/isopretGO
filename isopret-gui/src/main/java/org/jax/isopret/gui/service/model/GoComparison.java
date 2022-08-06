package org.jax.isopret.gui.service.model;

import org.jax.isopret.core.impl.go.GoMethod;
import org.jax.isopret.core.impl.go.MtcMethod;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.monarchinitiative.phenol.analysis.stats.GoTerm2PValAndCounts;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class is used to compare two sets of significant GO terms (DGE vs DAS).
 * It is intended to be used as the data for a paired bar chart in the GUI
 * @author Peter N Robinson
 */
public class GoComparison {

    private final static double ALPHA = 0.05;

    private final List<GoCompTerm> dgeGoCompTermList;
    private final List<GoCompTerm> dasGoCompTermList;

    private final List<GoCompTerm> goCompTermList;

    private final GoMethod goMethod;
    private final MtcMethod mtcMethod;


    public GoComparison(List<GoTerm2PValAndCounts> dge,
                        List<GoTerm2PValAndCounts> das,
                        Ontology ontology,
                        GoMethod goMethod,
                        MtcMethod mtcMethod) {
        this.goMethod = goMethod;
        this.mtcMethod = mtcMethod;
        // Collect all signficant GO Term ids
        Set<TermId> significantGoTerms =
                Stream.concat(dge.stream(), das.stream())
                        .filter(t -> t.getAdjustedPValue() <= ALPHA)
                        .map(GoTerm2PValAndCounts::getGoTermId)
                        .collect(Collectors.toSet());
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
        this.goCompTermList = List.copyOf(goCompTerms);
        List<GoCompTerm> dgeTerms = goCompTerms.stream().filter(GoCompTerm::dgePredominant).collect(Collectors.toList());
        List<GoCompTerm> dasTerms = goCompTerms.stream().filter(GoCompTerm::dasPredominant).collect(Collectors.toList());
        Collections.sort(dgeTerms);
        this.dgeGoCompTermList = List.copyOf(dgeTerms);
        Collections.sort(dasTerms);
        this.dasGoCompTermList = List.copyOf(dasTerms);
    }

    public List<GoCompTerm> getGoCompTermList() {
        return goCompTermList;
    }

    public List<GoCompTerm> getDgePredominentGoCompTerms() {
        return this.dgeGoCompTermList;
    }

    public List<GoCompTerm> getDasPredominentGoCompTerms() {
        return this.dasGoCompTermList;
    }

    public List<GoCompTerm> getDgeSignificant() {
        return this.dgeGoCompTermList.stream()
                .filter(GoCompTerm::dgeSignificant)
                .collect(Collectors.toList());
    }

    public List<GoCompTerm> getDasSignificant() {
        return this.dasGoCompTermList.stream()
                .filter(GoCompTerm::dasSignificant)
                .collect(Collectors.toList());
    }

    public String goMethod() {
        return this.goMethod.name();
    }

    public String mtcMethod() {
        return this.mtcMethod.name();
    }



}
