package org.jax.isopret.core.hbadeals;

import org.jax.isopret.core.transcript.AccessionNumber;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * THis class is used to perform Bayesian false discovery rate control.
 * The HBA-DEALS probability resulted for each gene expression assessment (or splicing) is
 * a Posterior Error Probability (PEP). By the linearity of expected value we
 * can add these probabilities at any threshold in order to get the total
 * expected number of false discoveries. If we thus rank the observations by PEP (from smallest to largest)
 * and choose the rank just before the rank where the cumulative mean of the FDR (called qvalue after John Storey),
 * this is where we set the threshold.
 */
public class HbaDealsThresholder {
    private static final Logger LOGGER = LoggerFactory.getLogger(HbaDealsThresholder.class);

    private static final double DEFAULT_THRESHOLD = 0.05;
    /** We will choose no gene with a higher probability of non-differentiality than this while calculating FDR. */
    private final double MAX_PROB = 0.25;
    /** Threshold for total probability to calculate Bayesian FDR (Usually, we will use 0.05). */
    private final double fdrThreshold;
    /** HBA-DEALS results for all genes in the experiment. */
    private final Map<String, HbaDealsResult> rawResults;
    /** Probability threshold for expression results that attains {@link #fdrThreshold} FDR for expression. */
    private final double expressionThreshold;
    /** Probability threshold for splicing results that attains {@link #fdrThreshold} FDR for splicing. */
    private final double splicingThreshold;

    private final Set<String> dgeGeneSymbols;

    private final Set<String> dasGeneSymbols;

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
        fdrThreshold = probThres;
        this.expressionThreshold = calculateExpressionThreshold();
        this.splicingThreshold = calculateSplicingThreshold();
        this.dgeGeneSymbols = this.rawResults
                .values()
                .stream()
                .filter(r -> r.getExpressionP() <= this.expressionThreshold)
                .map(HbaDealsResult::getSymbol)
                .collect(Collectors.toSet());
        this.dasGeneSymbols = this.rawResults
                .values()
                .stream()
                .filter(r -> r.getSmallestSplicingP() <= this.splicingThreshold)
                .map(HbaDealsResult::getSymbol)
                .collect(Collectors.toSet());
    }


    /**
     * Calculate a false discovery rate,
     * where the FDR is simply the sum of probabilities of not being differential of all items below threshold.
     * @param p probability threshold
     * @param probs probabilities of not being differential
     * @return estimated FDR at this probability threshold
     */
    private double getFdr(double p, List<Double> probs) {
        int i = 0;
        double fdr = 0.0;
        for (double prob : probs) {
            if (prob > p) {
                break;
            } else {
                fdr += prob;
                i++;
            }
        }
        if (i==0) return 0.0;
        return fdr/(double) i;
    }


    /**
     * Implements the following R
     * s <- seq(0.01,0.25,0.01) # 0.01, 0.02, ..., 0.25
     * # The following gets the sum of all P values not more than s[i] divided by the count of such instances
     * h0.de <- unlist(lapply(s,function(p)sum(res$P[res$Isoform=='Expression'& res$P<=p])/sum(res$Isoform=='Expression'& res$P<=p)))
     * exp.thresh <- s[max(which(h0.de<=fdr))] highest prob threshold below threshold.
     * Calculates the q-value threshold needed to attain a desired FDR
     * @param probs List of probabilities from HBA-DEALS
     * @return the probability threshold associated with the q-value threshold to reach the desired FDR
     */
    private double getThreshold(List<Double> probs) {
        double FDR_THRESHOLD = 0.05;
        Collections.sort(probs);
        double p_threshold = 0.0;
        double min_p = 0.0;
        double max_p = 0.25;
        double delta = max_p - min_p;
        double TOL = 0.001;
        while (delta > TOL) {
            double mid_p = min_p + (max_p - min_p)/2.0;
            double fdr = getFdr(mid_p, probs);
            if (fdr <= FDR_THRESHOLD) {
                min_p = mid_p;
                p_threshold = mid_p;
            } else {
                max_p = mid_p;
            }
            delta = max_p - min_p;
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
        return this.dgeGeneSymbols;
    }

    public Set<String> dasGeneSymbols() {
        return this.dasGeneSymbols;
    }

    public int getDgeGeneCount() {
        return this.dgeGeneSymbols.size();
    }

    public int getDasGeneCount() {
        return this.dasGeneSymbols.size();
    }



    public Set<String> population() {
        return this.rawResults.keySet();
    }

    public double getFdrThreshold() {
        return fdrThreshold;
    }

    public double getExpressionThreshold() {
        return expressionThreshold;
    }

    public double getSplicingThreshold() {
        return splicingThreshold;
    }

    public Map<String, HbaDealsResult> getRawResults() {
        return rawResults;
    }

    /**
     * @return all the ensembl gene Ids observed in our experiment, regardless of significant differential expr.
     */
    public Set<TermId> getAllGeneTermIds() {
        return rawResults.values().stream()
                .map(HbaDealsResult::getEnsgId)
                .map(AccessionNumber::ensgFromInt)
                .map(AccessionNumber::toTermId)
                .collect(Collectors.toSet());
    }

    /**
     * @return all the ensembl transcript Ids observed in our experiment, regardless of significant differential expr.
     */
    public Set<TermId> getAllTranscriptTermIds() {
        return rawResults.values().stream()
                .map(HbaDealsResult::getTranscriptMap)
                .map(Map::values)
                .flatMap(Collection::stream)
                .map(HbaDealsTranscriptResult::getTranscriptId)
                .map(AccessionNumber::toTermId)
                .collect(Collectors.toSet());
    }

    public Set<TermId> dgeGeneTermIds() {
        return this.rawResults
                .values()
                .stream()
                .filter(r -> r.getExpressionP() <= this.expressionThreshold)
                .map(HbaDealsResult::getGeneAccession)
                .map(AccessionNumber::toTermId)
                .collect(Collectors.toSet());
    }

    public Set<TermId> dasIsoformTermIds() {
        return rawResults.values().stream()
                .map(HbaDealsResult::getTranscriptMap)
                .map(Map::values)
                .flatMap(Collection::stream)
                .filter(r -> r.isSignificant(this.splicingThreshold ))
               .map(HbaDealsTranscriptResult::getTranscriptId)
                .map(AccessionNumber::toTermId)
                .collect(Collectors.toSet());
    }
}
