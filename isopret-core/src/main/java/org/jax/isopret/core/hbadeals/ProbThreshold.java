package org.jax.isopret.core.hbadeals;

import java.util.Collections;
import java.util.List;

public class ProbThreshold {

    private static final double DEFAULT_THRESHOLD = 0.05;
    /** We will choose no gene with a higher probability of non-differentiality than this while calculating FDR. */
    private static final double MAX_PROB = 0.25;
    /** Threshold for total probability to calculate Bayesian FDR (Usually, we will use 0.05). */
    private final double fdrThreshold;

    private final double qvalueThreshold;

    public ProbThreshold(List<Double> probabilities, double fdr) {
        this.fdrThreshold = fdr;
        this.qvalueThreshold = getThreshold(probabilities);
    }

    /**
     * Calculate FDR for the {@link #DEFAULT_THRESHOLD} value of the desired FDR
     * @param probabilities
     */
    public ProbThreshold(List<Double> probabilities) {
        this(probabilities, DEFAULT_THRESHOLD);
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
     * Together, this calculates the q-value threshold needed to attain a desired FDR
     * @param probs List of raw probabilities from HBA-DEALS
     * @return the probability threshold associated with the q-value threshold to reach the desired FDR
     */
    private double getThreshold(List<Double> probs) {
        Collections.sort(probs);
        double p_threshold = 0.0;
        double min_p = 0.0;
        double max_p = 0.25;
        double delta = max_p - min_p;
        double TOL = 0.001;
        while (delta > TOL) {
            double mid_p = min_p + (max_p - min_p)/2.0;
            double fdr = getFdr(mid_p, probs);
            if (fdr <= this.fdrThreshold) {
                min_p = mid_p;
                p_threshold = mid_p;
            } else {
                max_p = mid_p;
            }
            delta = max_p - min_p;
        }
        return p_threshold;
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


    public double getQvalueThreshold() {
        return qvalueThreshold;
    }

    public double getFdrThreshold() {
        return fdrThreshold;
    }
}
