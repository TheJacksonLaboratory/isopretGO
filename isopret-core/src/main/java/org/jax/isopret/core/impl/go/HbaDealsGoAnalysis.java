package org.jax.isopret.core.impl.go;


import org.jax.isopret.model.GoMethod;
import org.jax.isopret.model.MtcMethod;
import org.monarchinitiative.phenol.analysis.AssociationContainer;
import org.monarchinitiative.phenol.analysis.DirectAndIndirectTermAnnotations;
import org.monarchinitiative.phenol.analysis.StudySet;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.Term;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.monarchinitiative.phenol.analysis.stats.GoTerm2PValAndCounts;
import org.monarchinitiative.phenol.analysis.stats.ParentChildIntersectionPValueCalculation;
import org.monarchinitiative.phenol.analysis.stats.ParentChildUnionPValueCalculation;
import org.monarchinitiative.phenol.analysis.stats.TermForTermPValueCalculation;
import org.monarchinitiative.phenol.analysis.stats.mtc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class HbaDealsGoAnalysis {
    private final Logger LOGGER = LoggerFactory.getLogger(HbaDealsGoAnalysis.class);

    private final static double ALPHA = 0.05;

    private final Ontology ontology;

    private final StudySet study;
    private final StudySet population;
    private final GoMethod goMethod;

    private final MultipleTestingCorrection mtc;

    public HbaDealsGoAnalysis(Ontology ontology,
                              StudySet study,
                              StudySet population,
                              GoMethod method,
                              MtcMethod mtcMethod) {
        this.ontology = ontology;
        this.goMethod = method;
        switch (mtcMethod) {
            case BONFERRONI -> mtc = new Bonferroni();
            case BONFERRONI_HOLM -> mtc = new BonferroniHolm();
            case BENJAMINI_HOCHBERG -> mtc = new BenjaminiHochberg();
            case BENJAMINI_YEKUTIELI -> mtc = new BenjaminiYekutieli();
            case SIDAK -> mtc = new Sidak();
            case NONE -> mtc = new NoMultipleTestingCorrection();
            default -> {
                // should never happen
                System.err.println("[WARNING] Did not recognize MTC");
                mtc = new Bonferroni();
            }
        }
        this.study = study;
        this.population = population;
    }

    public List<GoTerm2PValAndCounts> overrepresetationAnalysis() {
        List<GoTerm2PValAndCounts> pvals = switch (this.goMethod) {
            case TFT -> termForTerm();
            case PCunion -> parentChildUnion();
            case PCintersect -> parentChildIntersect();
        };
        pvals.sort(new SortByPvalue());
        return List.copyOf(pvals); // make immutable
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
                mtc);
        return tftpvalcal.calculatePVals()
                .stream()
                .filter(item -> item.passesThreshold(ALPHA))
                .collect(Collectors.toList());
    }

    private List<GoTerm2PValAndCounts> parentChildUnion() {
        ParentChildUnionPValueCalculation tftpvalcal = new ParentChildUnionPValueCalculation(this.ontology,
                this.population,
                this.study,
                mtc);
        return tftpvalcal.calculatePVals()
                .stream()
                .filter(item -> item.passesThreshold(ALPHA))
                .collect(Collectors.toList());
    }

    private List<GoTerm2PValAndCounts> parentChildIntersect() {
        ParentChildIntersectionPValueCalculation tftpvalcal = new ParentChildIntersectionPValueCalculation(this.ontology,
                this.population,
                this.study,
                mtc);
        return tftpvalcal.calculatePVals()
                .stream()
                .filter(item -> item.passesThreshold(ALPHA))
                .collect(Collectors.toList());
    }








    public static HbaDealsGoAnalysis termForTerm(Ontology ontology,
                                                 AssociationContainer<TermId> associationContainer,
                                                 StudySet study,
                                                 StudySet population,
                                                 MtcMethod mtcMethod) {
        return new HbaDealsGoAnalysis(ontology, study, population, GoMethod.TFT, mtcMethod);
    }

    public static HbaDealsGoAnalysis parentChildUnion(Ontology ontology,
                                                      AssociationContainer<TermId> associationContainer,
                                                      StudySet study,
                                                      StudySet population,
                                                      MtcMethod mtcMethod) {
        return new HbaDealsGoAnalysis(ontology, study, population, GoMethod.PCunion, mtcMethod);
    }

    public static HbaDealsGoAnalysis parentChildIntersect(Ontology ontology,
                                                          AssociationContainer<TermId> associationContainer,
                                                          StudySet study,
                                                          StudySet population,
                                                          MtcMethod mtcMethod) {
        return new HbaDealsGoAnalysis(ontology, study, population, GoMethod.PCintersect, mtcMethod);
    }




    /**
     * Given a list of enriched GO terms and the list of genes in the study set that are
     * enriched in these terms, this function creates a map with
     * Key -- gene symbols for any genes that are annotated to enriched GO terms.
     * Value -- list of corresponding annotations, but only to the enriched GO terms
     */
    public Map<TermId, Set<Term>> getEnrichedSymbolToEnrichedGoMap(Set<TermId> einrichedGoTermIdSet) {
        Map<TermId, Set<Term>> symbolToGoTermResults = new HashMap<>();
        for (DirectAndIndirectTermAnnotations dai : study.getAnnotationMap().values()) {
            TermId ontologyId = dai.getOntologyId();
            if (! ontology.containsTerm(ontologyId)) {
                LOGGER.error("Could not find Ontology id {}", ontologyId.getValue());
                continue;
            }
            Optional<Term> opt = ontology.termForTermId(ontologyId);
            if (opt.isEmpty()) {
                LOGGER.error("Could not retrieve term for {}", ontologyId);
            } else {
                Term term = opt.get();
                for (TermId domainItemId : dai.getTotalAnnotatedDomainItemSet()) {
                    symbolToGoTermResults.computeIfAbsent(domainItemId, r -> new HashSet<>()).add(term);
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
