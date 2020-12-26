package org.jax.isopret.hbadeals;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class HbaDealsThresholder {


    private static final double DEFAULT_THRESHOLD = 0.05;

    private final double MAX_PROB = 0.25;
    /** Threshold for total probability to calculate Bayesian FDR. */
    private final double probabilityThreshold;
    /** HBA-DEALS results for all genes in the experiment. */
    private final Map<String, HbaDealsResult> rawResults;
    /** Probability threshold for expression results that attains {@link #probabilityThreshold} FDR for expression. */
    private final double expressionThreshold;
    /** Probability threshold for splicing results that attains {@link #probabilityThreshold} FDR for splicing. */
    private final double splicingThreshold;

    /**
     * Find the FDR thresholds for splicing and expression
     * @param results Map of HBA-DEALS analysis results (key: gene symbol)
     */
    public HbaDealsThresholder(Map<String, HbaDealsResult> results) {
       this(results, DEFAULT_THRESHOLD);
    }

    /**
     * Find the FDR thresholds for splicing and expression
     * @param results Map of HBA-DEALS analysis results (key: gene symbol)
     */
    public HbaDealsThresholder(Map<String, HbaDealsResult> results, double probThres) {
        rawResults = results;
        probabilityThreshold = probThres;
        this.expressionThreshold = calculateExpressionThreshold();
        this.splicingThreshold = calculateSplicingThreshold();
    }


    private double getThreshold( List<Double> probs) {
        Collections.sort(probs);
        double cumSum = 0.0;
        double p_threshold = 0.0;
        int count = 0;
        for (double p : probs) {
            if (p > MAX_PROB) break;
            count++;
            cumSum += p;
            if (cumSum/count > probabilityThreshold) break;
            p_threshold = p;
        }
        return p_threshold;
    }

    private double calculateExpressionThreshold() {
        List<Double> expressionProbs = rawResults
                .values()
                .stream()
                .map(HbaDealsResult::getExpressionP)
                .collect(Collectors.toList());
       return getThreshold(expressionProbs);
    }

    private double calculateSplicingThreshold() {
        List<Double> splicingProbs = rawResults
                .values()
                .stream()
                .map(HbaDealsResult::getSplicingPlist)
                .flatMap(List::stream)
                .collect(Collectors.toList());
        return getThreshold(splicingProbs);
    }

    public int getTotalGeneCount() {
        return this.rawResults.size();
    }

    public Set<String> dgeGeneSymbols() {
        return this.rawResults
                .values()
                .stream()
                .filter(r -> r.getExpressionP() < this.expressionThreshold)
                .map(HbaDealsResult::getSymbol)
                .collect(Collectors.toSet());
    }

    public Set<String> dasGeneSymbols() {
        return this.rawResults
                .values()
                .stream()
                .filter(r -> r.getMostSignificantSplicingPval() < this.splicingThreshold)
                .map(HbaDealsResult::getSymbol)
                .collect(Collectors.toSet());
    }

    public Set<String> dasDgeGeneSymbols() {
        return this.rawResults
                .values()
                .stream()
                .filter(r -> r.getExpressionP() < this.expressionThreshold)
                .filter(r -> r.getMostSignificantSplicingPval() < this.splicingThreshold)
                .map(HbaDealsResult::getSymbol)
                .collect(Collectors.toSet());
    }

    public Set<String> population() {
        return this.rawResults.keySet();
    }

    public double getProbabilityThreshold() {
        return probabilityThreshold;
    }

    public double getExpressionThreshold() {
        return expressionThreshold;
    }

    public double getSplicingThreshold() {
        return splicingThreshold;
    }
}
