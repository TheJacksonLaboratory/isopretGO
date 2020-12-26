package org.jax.isopret.go;

import org.jax.isopret.except.IsopretRuntimeException;
import org.jax.isopret.hbadeals.HbaDealsResult;

import org.jax.isopret.hbadeals.HbaDealsThresholder;
import org.monarchinitiative.phenol.analysis.GoAssociationContainer;
import org.monarchinitiative.phenol.analysis.ItemAssociations;
import org.monarchinitiative.phenol.analysis.StudySet;
import org.monarchinitiative.phenol.analysis.mgsa.MgsaCalculation;
import org.monarchinitiative.phenol.annotations.formats.go.GoGaf21Annotation;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermAnnotation;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.monarchinitiative.phenol.stats.GoTerm2PValAndCounts;
import org.monarchinitiative.phenol.stats.ParentChildIntersectionPValueCalculation;
import org.monarchinitiative.phenol.stats.ParentChildUnionPValueCalculation;
import org.monarchinitiative.phenol.stats.TermForTermPValueCalculation;
import org.monarchinitiative.phenol.stats.mtc.Bonferroni;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class HbaDealsGoAnalysis {

    private final static double ALPHA = 0.05;

    private final Ontology ontology;
    private final GoAssociationContainer goAssociationContainer;

    private final StudySet dge;
    private final StudySet das;
    private final StudySet dasDge;
    private final StudySet population;
    private final GoMethod method;

    enum GoMethod { TFT, PCunion, PCintersect, MGSA}


    private HbaDealsGoAnalysis(HbaDealsThresholder thresholder,
                              Ontology ontology,
                              GoAssociationContainer associationContainer,
                               GoMethod method) {
        this.ontology = ontology;
        this.goAssociationContainer = associationContainer;
        this.method = method;
        Set<String> population = thresholder.population();
        Set<String> dgeGenes = thresholder.dgeGeneSymbols();
        Set<String> dasGenes = thresholder.dasGeneSymbols();
        Set<String> dasDgeGenes = thresholder.dasDgeGeneSymbols();
        this.dge = associationContainer.fromGeneSymbols(dgeGenes, "dge");
        this.das = associationContainer.fromGeneSymbols(dasGenes, "das");
        this.dasDge = associationContainer.fromGeneSymbols(dasDgeGenes, "dasdge");
        this.population = associationContainer.fromGeneSymbols(population, "population");
    }


    public int populationCount() { return this.population.getAnnotatedItemCount(); }
    public int dasCount() { return  this.das.getAnnotatedItemCount(); }
    public int unmappedDasCount() { return this.das.getUnmappedGeneSymbolCount(); }
    public List<String> unmappedDasSymbols() { return this.das.getSortedUnmappedGeneSymbols(); }
    public int dgeCount() { return this.dge.getAnnotatedItemCount(); }
    public int unmappedDgeCount() { return this.dge.getUnmappedGeneSymbolCount(); }
    public List<String> unmappedDgeSymbols() { return this.dge.getSortedUnmappedGeneSymbols(); }
    public int dasDgeCount() { return this.dasDge.getAnnotatedItemCount(); }
    public int unmappedDasDgeCount() { return this.dasDge.getUnmappedGeneSymbolCount(); }
    public List<String> unmappedDasDgeSymbols() { return this.dasDge.getSortedUnmappedGeneSymbols(); }



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
        switch (this.method) {
            case TFT:
                return termForTerm(this.dge);
            case PCunion:
                return parentChildUnion(this.dge);
            case PCintersect:
                return parentChildIntersect(this.dge);
            case MGSA:
                return mgsa(this.dge);
            default:
                // should never happen
                throw new IsopretRuntimeException("Unrecognized method");
        }
    }

    public List<GoTerm2PValAndCounts> dasOverrepresetationAnalysis() {
        switch (this.method) {
            case TFT:
                return termForTerm(this.das);
            case PCunion:
                return parentChildUnion(this.das);
            case PCintersect:
                return parentChildIntersect(this.das);
            case MGSA:
                return mgsa(this.das);
            default:
                // should never happen
                throw new IsopretRuntimeException("Unrecognized method");
        }
    }

    public List<GoTerm2PValAndCounts> dasDgeOverrepresetationAnalysis() {
        switch (this.method) {
            case TFT:
                return termForTerm(this.dasDge);
            case PCunion:
                return parentChildUnion(this.dasDge);
            case PCintersect:
                return parentChildIntersect(this.dasDge);
            case MGSA:
                return mgsa(this.das);
            default:
                // should never happen
                throw new IsopretRuntimeException("Unrecognized method");
        }
    }



    public static HbaDealsGoAnalysis termForTerm(HbaDealsThresholder thresholder,
                                                 Ontology ontology,
                                                 GoAssociationContainer associationContainer) {
        return new HbaDealsGoAnalysis(thresholder, ontology, associationContainer, GoMethod.TFT);
    }

    public static HbaDealsGoAnalysis parentChildUnion(HbaDealsThresholder thresholder,
                                                 Ontology ontology,
                                                 GoAssociationContainer associationContainer) {
        return new HbaDealsGoAnalysis(thresholder, ontology, associationContainer, GoMethod.PCunion);
    }

    public static HbaDealsGoAnalysis parentChildIntersect(HbaDealsThresholder thresholder,
                                                 Ontology ontology,
                                                 GoAssociationContainer associationContainer) {
        return new HbaDealsGoAnalysis(thresholder, ontology, associationContainer, GoMethod.PCintersect);
    }

    public static HbaDealsGoAnalysis mgssa(HbaDealsThresholder thresholder,
                                                          Ontology ontology,
                                                          GoAssociationContainer associationContainer) {
        return new HbaDealsGoAnalysis(thresholder, ontology, associationContainer, GoMethod.MGSA);
    }


    /**
     * Given a list of enriched GO terms and the list of genes in the study set that are
     * enriched in these terms, this function creates a map with
     * Key -- gene symbols for any genes that are annotated to enriched GO terms.
     * Value -- list of correspondings annotations, but only to the enriched GO terms
     */
    public Map<String, List<GoTermIdPlusLabel>> getEnrichedSymbolToEnrichedGoMap (Set<TermId> einrichedGoTermIdSet, Set<String> symbols) {
        Map<String, List<GoTermIdPlusLabel>> symbolToGoTermResults = new HashMap<>();
        List<GoGaf21Annotation> rawAnnots = this.goAssociationContainer.getRawAssociations();
        Map<String, ItemAssociations> tempMap = new HashMap<>();
        for (GoGaf21Annotation a : rawAnnots) {
            String symbol = a.getDbObjectSymbol();
            if (symbols.contains(symbol)) {
                symbolToGoTermResults.putIfAbsent(symbol, new ArrayList<>());
                TermId goId = a.getGoId();
                if (einrichedGoTermIdSet.contains(goId) && ontology.getTermMap().containsKey(goId)) {
                    String label = ontology.getTermMap().get(goId).getName();
                    symbolToGoTermResults.get(symbol).add(new GoTermIdPlusLabel(goId, label));
                }
            }
        }
        return symbolToGoTermResults;
    }

}
