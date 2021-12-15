package org.jax.isopret.core.go;

import org.jax.isopret.core.except.IsopretRuntimeException;

import org.jax.isopret.core.hbadeals.HbaDealsThresholder;
import org.monarchinitiative.phenol.analysis.GoAssociationContainer;
import org.monarchinitiative.phenol.analysis.StudySet;
import org.monarchinitiative.phenol.annotations.formats.go.GoGaf21Annotation;
import org.monarchinitiative.phenol.ontology.data.Ontology;
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

    private final StudySet dge;
    private final StudySet das;
    private final StudySet population;
    private final GoMethod goMethod;
    private final MultipleTestingCorrection mtc;
    private final GoAssociationContainer goAssociationContainer;


    private HbaDealsGoAnalysis(HbaDealsThresholder thresholder,
                               Ontology ontology,
                               GoAssociationContainer associationContainer,
                               GoMethod method,
                               MtcMethod mtcMethod) {
        this.ontology = ontology;
        this.goMethod = method;
        this.goAssociationContainer = associationContainer;
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
        Set<String> population = thresholder.population();
        Set<String> dgeGenes = thresholder.dgeGeneSymbols();
        Set<String> dasGenes = thresholder.dasGeneSymbols();
        this.dge = associationContainer.fromGeneSymbols(dgeGenes, "dge");
        this.das = associationContainer.fromGeneSymbols(dasGenes, "das");
        this.population = associationContainer.fromGeneSymbols(population, "population");
    }


    public int populationCount() {
        return this.population.getAnnotatedItemCount();
    }

    public int dasCount() {
        return this.das.getAnnotatedItemCount();
    }

    public int unmappedDasCount() {
        return this.das.getUnmappedGeneSymbolCount();
    }

    public List<String> unmappedDasSymbols() {
        return this.das.getSortedUnmappedGeneSymbols();
    }

    public int dgeCount() {
        return this.dge.getAnnotatedItemCount();
    }

    public int unmappedDgeCount() {
        return this.dge.getUnmappedGeneSymbolCount();
    }

    public List<String> unmappedDgeSymbols() {
        return this.dge.getSortedUnmappedGeneSymbols();
    }


    private List<GoTerm2PValAndCounts> termForTerm(StudySet study) {
        TermForTermPValueCalculation tftpvalcal = new TermForTermPValueCalculation(this.ontology,
                this.population,
                study,
                new Bonferroni());
        return tftpvalcal.calculatePVals()
                .stream()
                .filter(item -> item.passesThreshold(ALPHA))
                .collect(Collectors.toList());
    }

    private List<GoTerm2PValAndCounts> parentChildUnion(StudySet study) {
        ParentChildUnionPValueCalculation tftpvalcal = new ParentChildUnionPValueCalculation(this.ontology,
                this.population,
                study,
                new Bonferroni());
        return tftpvalcal.calculatePVals()
                .stream()
                .filter(item -> item.passesThreshold(ALPHA))
                .collect(Collectors.toList());
    }

    private List<GoTerm2PValAndCounts> parentChildIntersect(StudySet study) {
        ParentChildIntersectionPValueCalculation tftpvalcal = new ParentChildIntersectionPValueCalculation(this.ontology,
                this.population,
                study,
                new Bonferroni());
        return tftpvalcal.calculatePVals()
                .stream()
                .filter(item -> item.passesThreshold(ALPHA))
                .collect(Collectors.toList());
    }

    private List<GoTerm2PValAndCounts> mgsa(StudySet study) {
        throw new UnsupportedOperationException(); // TODO
    }


    public List<GoTerm2PValAndCounts> dgeOverrepresetationAnalysis() {
        return switch (this.goMethod) {
            case TFT -> termForTerm(this.dge);
            case PCunion -> parentChildUnion(this.dge);
            case PCintersect -> parentChildIntersect(this.dge);
            case MGSA -> mgsa(this.dge);
        };
    }

    public List<GoTerm2PValAndCounts> dasOverrepresetationAnalysis() {
        return switch (this.goMethod) {
            case TFT -> termForTerm(this.das);
            case PCunion -> parentChildUnion(this.das);
            case PCintersect -> parentChildIntersect(this.das);
            case MGSA -> mgsa(this.das);
        };
    }



    public static HbaDealsGoAnalysis termForTerm(HbaDealsThresholder thresholder,
                                                 Ontology ontology,
                                                 GoAssociationContainer associationContainer,
                                                 MtcMethod mtcMethod) {
        return new HbaDealsGoAnalysis(thresholder, ontology, associationContainer, GoMethod.TFT, mtcMethod);
    }

    public static HbaDealsGoAnalysis parentChildUnion(HbaDealsThresholder thresholder,
                                                      Ontology ontology,
                                                      GoAssociationContainer associationContainer, MtcMethod mtcMethod) {
        return new HbaDealsGoAnalysis(thresholder, ontology, associationContainer, GoMethod.PCunion, mtcMethod);
    }

    public static HbaDealsGoAnalysis parentChildIntersect(HbaDealsThresholder thresholder,
                                                          Ontology ontology,
                                                          GoAssociationContainer associationContainer,
                                                          MtcMethod mtcMethod) {
        return new HbaDealsGoAnalysis(thresholder, ontology, associationContainer, GoMethod.PCintersect, mtcMethod);
    }

    public static HbaDealsGoAnalysis mgsa(HbaDealsThresholder thresholder,
                                          Ontology ontology,
                                          GoAssociationContainer associationContainer,
                                          MtcMethod mtcMethod) {
        return new HbaDealsGoAnalysis(thresholder, ontology, associationContainer, GoMethod.MGSA, mtcMethod);
    }


    /**
     * Given a list of enriched GO terms and the list of genes in the study set that are
     * enriched in these terms, this function creates a map with
     * Key -- gene symbols for any genes that are annotated to enriched GO terms.
     * Value -- list of corresponding annotations, but only to the enriched GO terms
     */
    public Map<String, Set<GoTermIdPlusLabel>> getEnrichedSymbolToEnrichedGoMap(Set<TermId> einrichedGoTermIdSet, Set<String> symbols) {
        Map<String, Set<GoTermIdPlusLabel>> symbolToGoTermResults = new HashMap<>();
        List<GoGaf21Annotation> rawAnnots = this.goAssociationContainer.getRawAssociations();
        for (GoGaf21Annotation a : rawAnnots) {
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
