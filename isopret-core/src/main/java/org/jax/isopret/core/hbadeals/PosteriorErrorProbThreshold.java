package org.jax.isopret.core.hbadeals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

/**
 * This class is used to calculate the threshold posterior error proibability (PEP) threshold that achieves a desired
 * false-discovery rate (FDR) over the current dataset. Explanations can be found in
 * Käll L, Storey JD, MacCoss MJ, Noble WS. Posterior error probabilities and false discovery rates:
 * two sides of the same coin. J Proteome Res. 2008 Jan;7(1):40-4. PMID:18052118.
 * In brief, the The q-value of a test measures the proportion of false positives incurred (called the false discovery
 * rate) when that particular test is called significant (thus, the q-value is the minimum FDR at which the test would be
 * called significant in the context of the current dataset). The local FDR (another name for the PEP) measures the
 * posterior probability the null hypothesis is true given the test's p-value. Whereas the FDR measures the error rate
 * associated with a collection of tests, the PEP measures the probability of error for a single test.
 * The mean value of all PEPs that are called significant corresponds to the FDR at the corresponding threshold.
 * Our goal is to set the FDR to a given threshold (by default, 0.01) and to define the PEP threshold that calls
 * as many tests as possible signficant while still maintaining this threshold. We apply the additional criterion that
 * if the PEP is larger than 0.25 the test is not called significant, regardless of the FDR.
 */
public class PosteriorErrorProbThreshold {

    private static final double DEFAULT_FDR_THRESHOLD = 0.01;
    /** We will choose no gene with a higher PEP than this while calculating FDR. */
    private static final double MAX_PEP = 0.25;
    /** Threshold for total probability to calculate Bayesian FDR (Default is {@link #DEFAULT_FDR_THRESHOLD}). */
    private final double fdrThreshold;
    /** The list of posterior error probabilities calculated for the various tests done in the current dataset (in our
     * case, the tests are for either differential gene expression or differential isoform expression).
     */
    private final List<Double> probabilities;

    public PosteriorErrorProbThreshold(List<Double> pepValues, double fdr) {
        this.fdrThreshold = fdr;
        Collections.sort(pepValues);
        this.probabilities = pepValues;
    }

    /**
     * Calculate FDR for the {@link #DEFAULT_FDR_THRESHOLD} value of the desired FDR
     * @param probabilities HBADEALS probabilities (PEP)
     */
    public PosteriorErrorProbThreshold(List<Double> probabilities) {
        this(probabilities, DEFAULT_FDR_THRESHOLD);
    }



    /**
     * Implements the following R
     * # s containst the possible thresholds for the adjusted p-value
     * s <- seq(0.01,0.25,0.01) # 0.01, 0.02, ..., 0.25
     * # pvals contains the observed (raw) p-values
     * pvals <- c(... raw p-value list .. )
     * fdr <- 0.05 # desired false-discovery rate
     * # The following gets the sum of all P values not more than s[i] divided by the count of such instances
     * # Note that unlist producse a vector which contains all the atomic components of the original list
     * h0.de <- unlist(lapply(s,function(p) {sum(pvals[pvals<=p])/sum(pvals<=p)} ))
     * exp.thresh <- s[max(which(h0.de<=fdr))] highest prob threshold below threshold.
     * Together, this calculates the posterior error probability (PEP) threshold needed to attain a desired FDR
     * @return the posterior error probability (PEP) threshold associated with the q-value threshold to reach the desired FDR
     */
    public double getPepThresholdBisection() {
        Collections.sort(this.probabilities);
        double p_threshold = 0.0;
        double min_p = 0.0;
        double max_p = 0.25;
        double delta = max_p - min_p;
        double TOL = 0.001;
        while (delta > TOL) {
            double mid_p = min_p + (max_p - min_p)/2.0;
            double fdr = getFdr(mid_p, this.probabilities);
            if (fdr <= this.fdrThreshold) {
                min_p = mid_p;
                p_threshold = mid_p;
            } else {
                max_p = mid_p;
            }
            delta = max_p - min_p;
        }
        double finalP_threshold = p_threshold;
        // we will always accept PEP of zero ("no error probability") and so if
        // no values pass our threshold it is always safe to return zero (to satisfy the API), even
        // though we do not have any actual values of zero
        double pep =  this.probabilities.stream().filter(p -> p <= finalP_threshold).mapToDouble(p->p).max().orElse(0);
        return Math.min(pep, MAX_PEP);
    }


    /**
     *  # Look for a p-val threshold between 0.01 and 0.25
     *   s <- seq(0.01,0.25,0.01)
     *   # sum(vals<=p) gives the number of values below or equal to p
     *   # sum(vals[vals<=p]) gives the sum of vals
     *   # the quotient returns the mean of all vals below p
     *   h0.de <- unlist(lapply(s,function(p) { sum(vals[vals<=p])/sum(vals<=p) }))
     *   # which(h0.de<=fdr) returns the indices for which h0.de<=fdr
     *   # by design this is a series such as 1 2 ... k
     *   # max(which(h0.de<=fdr)) returns the largest (rightmost) such index
     *   # s[...] returns the corresponding value of the sequence of 0.01, 0.02,..,0.25
     *   exp.thresh <- s[max(which(h0.de<=fdr))]
     * @return posterior error probability threshold
     */
    public double getPepThreshold() {
        Collections.sort(this.probabilities);
        // get a sequence of numbers between 0.01 and 0.25 -- these
        // are the candidate PEP thresholds
        List<Double> s = IntStream.iterate(1, i -> i + 1)
                .limit(25)
                .boxed()
                .map(d -> 0.01 * d)
                .toList();
        List<Double> h0de = new ArrayList<>();
        for (double candidatePepThreshold : s) {
            double sumValues = 0d;
            int nBelowThresh = 0;
            for (double p : probabilities) {
                if (p < candidatePepThreshold) {
                    sumValues += p;
                    nBelowThresh++;
                } else {
                    break;
                }
            }
            h0de.add(sumValues/nBelowThresh);
        }
        for (int i=0;i<h0de.size(); ++i) {
            if (h0de.get(i) > this.fdrThreshold) {
                if (i>0) {
                    // return largest prethreshold value
                    return s.get(i-1);
                } else {
                    return 0; // cannot reach FDR with current results
                }
            }
        }
        // if we get here, we are at our max allowable PEP threshold of 0.25
        return MAX_PEP;
    }

    /**
     * Calculate a false discovery rate,
     * where the FDR is simply the sum of probabilities of
     * not being differential of all items below threshold.
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

}
