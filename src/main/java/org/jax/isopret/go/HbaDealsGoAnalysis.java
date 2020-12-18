package org.jax.isopret.go;

import org.jax.isopret.hbadeals.HbaDealsResult;

import org.monarchinitiative.phenol.analysis.GoAssociationContainer;
import org.monarchinitiative.phenol.analysis.StudySet;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.stats.GoTerm2PValAndCounts;
import org.monarchinitiative.phenol.stats.TermForTermPValueCalculation;
import org.monarchinitiative.phenol.stats.mtc.Bonferroni;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class HbaDealsGoAnalysis {

    private final static double ALPHA = 0.05;

    private final Ontology ontology;
    private final GoAssociationContainer goAssociationContainer;

    private final StudySet dge;
    private final StudySet das;
    private final StudySet population;


    public HbaDealsGoAnalysis(Map<String, HbaDealsResult> hbaDealsResultMap,
                              Ontology ontology,
                              GoAssociationContainer associationContainer) {
        this.ontology = ontology;
        this.goAssociationContainer = associationContainer;

        Set<String> population = new HashSet<>();
        Set<String> dgeGenes = new HashSet<>();
        Set<String> dasGenes = new HashSet<>();
        for (var result : hbaDealsResultMap.values()) {
            String geneSymbol = result.getSymbol();
            population.add(geneSymbol);
            if (result.hasSignificantExpressionResult()) {
                dgeGenes.add(geneSymbol);
            }
            if (result.hasaSignificantSplicingResult()) {
                dasGenes.add(geneSymbol);
            }
        }
        this.dge = associationContainer.fromGeneSymbols(dgeGenes, "dge", ontology);
        this.das = associationContainer.fromGeneSymbols(dasGenes, "das", ontology);
        this.population = associationContainer.fromGeneSymbols(population, "population", ontology);
    }

   public List<GoTerm2PValAndCounts> dgeOverrepresetationAnalysis() {
        TermForTermPValueCalculation tftpvalcal = new TermForTermPValueCalculation(this.ontology,
                this.population,
                this.dge,
                new Bonferroni());
        return tftpvalcal.calculatePVals()
                .stream()
                .filter(item -> item.passesThreshold(ALPHA))
                .collect(Collectors.toList());
    }

    public List<GoTerm2PValAndCounts> dasOverrepresetationAnalysis() {
        TermForTermPValueCalculation tftpvalcal = new TermForTermPValueCalculation(this.ontology,
                this.population,
                this.dge,
                new Bonferroni());
        return tftpvalcal.calculatePVals()
                .stream()
                .filter(item -> item.passesThreshold(ALPHA))
                .collect(Collectors.toList());
    }

    public int populationCount() {
        return this.population.getAnnotatedItemCount();
    }


}
