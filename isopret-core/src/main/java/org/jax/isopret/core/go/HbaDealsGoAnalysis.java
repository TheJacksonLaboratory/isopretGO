package org.jax.isopret.core.go;


import org.monarchinitiative.phenol.analysis.AssociationContainer;
import org.monarchinitiative.phenol.analysis.StudySet;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermAnnotation;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.monarchinitiative.phenol.stats.GoTerm2PValAndCounts;
import org.monarchinitiative.phenol.stats.ParentChildIntersectionPValueCalculation;
import org.monarchinitiative.phenol.stats.ParentChildUnionPValueCalculation;
import org.monarchinitiative.phenol.stats.TermForTermPValueCalculation;
import org.monarchinitiative.phenol.stats.mtc.*;

import java.util.*;
import java.util.stream.Collectors;

public class HbaDealsGoAnalysis {

    private final static double ALPHA = 0.05;

    private final Ontology ontology;

    private final StudySet study;
    private final StudySet population;
    private final GoMethod goMethod;
    private final MultipleTestingCorrection mtc;
    private final AssociationContainer<TermId> associationContainer;


    private HbaDealsGoAnalysis(Ontology ontology,
                               AssociationContainer<TermId> associationContainer,
                               StudySet study,
                               StudySet population,
                               GoMethod method,
                               MtcMethod mtcMethod) {
        this.ontology = ontology;
        this.associationContainer = associationContainer;
        this.goMethod = method;
        switch (mtcMethod) {
            case BONFERRONI -> this.mtc = new Bonferroni();
            case BONFERRONI_HOLM -> this.mtc = new BonferroniHolm();
            case BENJAMINI_HOCHBERG -> this.mtc = new BenjaminiHochberg();
            case BENJAMINI_YEKUTIELI -> this.mtc = new BenjaminiYekutieli();
            case SIDAK -> this.mtc = new Sidak();
            case NONE -> this.mtc = new NoMultipleTestingCorrection();
            default -> {
                // should never happen
                System.err.println("[WARNING] Did not recognize MTC");
                this.mtc = new Bonferroni();
            }
        }
        this.study = study;
        this.population = population;
    }


    public int populationCount() {
        return this.population.getAnnotatedItemCount();
    }

    public int studyCount() {
        return this.study.getAnnotatedItemCount();
    }




    private List<GoTerm2PValAndCounts> termForTerm() {
        TermForTermPValueCalculation tftpvalcal = new TermForTermPValueCalculation(this.ontology,
                this.population,
                this.study,
                new Bonferroni());
        return tftpvalcal.calculatePVals()
                .stream()
                .filter(item -> item.passesThreshold(ALPHA))
                .collect(Collectors.toList());
    }

    private List<GoTerm2PValAndCounts> parentChildUnion() {
        ParentChildUnionPValueCalculation tftpvalcal = new ParentChildUnionPValueCalculation(this.ontology,
                this.population,
                this.study,
                new Bonferroni());
        return tftpvalcal.calculatePVals()
                .stream()
                .filter(item -> item.passesThreshold(ALPHA))
                .collect(Collectors.toList());
    }

    private List<GoTerm2PValAndCounts> parentChildIntersect() {
        ParentChildIntersectionPValueCalculation tftpvalcal = new ParentChildIntersectionPValueCalculation(this.ontology,
                this.population,
                this.study,
                new Bonferroni());
        return tftpvalcal.calculatePVals()
                .stream()
                .filter(item -> item.passesThreshold(ALPHA))
                .collect(Collectors.toList());
    }

    private List<GoTerm2PValAndCounts> mgsa() {
        throw new UnsupportedOperationException(); // TODO
    }


    public List<GoTerm2PValAndCounts> dgeOverrepresetationAnalysis() {
        return switch (this.goMethod) {
            case TFT -> termForTerm();
            case PCunion -> parentChildUnion();
            case PCintersect -> parentChildIntersect();
            case MGSA -> mgsa();
        };
    }

    public List<GoTerm2PValAndCounts> dasOverrepresetationAnalysis() {
        return switch (this.goMethod) {
            case TFT -> termForTerm();
            case PCunion -> parentChildUnion();
            case PCintersect -> parentChildIntersect();
            case MGSA -> mgsa();
        };
    }



    public static HbaDealsGoAnalysis termForTerm(Ontology ontology,
                                                 AssociationContainer<TermId> associationContainer,
                                                 StudySet study,
                                                 StudySet population,
                                                 MtcMethod mtcMethod) {
        return new HbaDealsGoAnalysis(ontology, associationContainer, study, population, GoMethod.TFT, mtcMethod);
    }

    public static HbaDealsGoAnalysis parentChildUnion(Ontology ontology,
                                                      AssociationContainer<TermId> associationContainer,
                                                      StudySet study,
                                                      StudySet population,
                                                      MtcMethod mtcMethod) {
        return new HbaDealsGoAnalysis(ontology, associationContainer, study, population, GoMethod.PCunion, mtcMethod);
    }

    public static HbaDealsGoAnalysis parentChildIntersect(Ontology ontology,
                                                          AssociationContainer<TermId> associationContainer,
                                                          StudySet study,
                                                          StudySet population,
                                                          MtcMethod mtcMethod) {
        return new HbaDealsGoAnalysis(ontology,  associationContainer, study, population, GoMethod.PCintersect, mtcMethod);
    }

    public static HbaDealsGoAnalysis mgsa(Ontology ontology,
                                          AssociationContainer<TermId> associationContainer,
                                          StudySet study,
                                          StudySet population,
                                          MtcMethod mtcMethod) {
        return new HbaDealsGoAnalysis(ontology, associationContainer, study, population, GoMethod.MGSA, mtcMethod);
    }


    /**
     * Given a list of enriched GO terms and the list of genes in the study set that are
     * enriched in these terms, this function creates a map with
     * Key -- gene symbols for any genes that are annotated to enriched GO terms.
     * Value -- list of corresponding annotations, but only to the enriched GO terms
     */
    public Map<String, Set<GoTermIdPlusLabel>> getEnrichedSymbolToEnrichedGoMap(Set<TermId> einrichedGoTermIdSet, Set<String> symbols) {
        Map<String, Set<GoTermIdPlusLabel>> symbolToGoTermResults = new HashMap<>();
        List<TermAnnotation> rawAnnots = this.associationContainer.getRawAssociations();
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

    public StudySet getStudy() {
        return study;
    }

    public StudySet getPopulation() {
        return population;
    }
}
