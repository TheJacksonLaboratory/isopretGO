package org.jax.isopret.hbadeals;

import org.jax.isopret.core.impl.rnaseqdata.PosteriorErrorProbThreshold;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/*
c(0.00014, 0.94042, 0.73153, 0.36663, 0.32397, 0.00062, 0.55860, 0.58922, 0.00079, 0.00010, 0.00209, 0.81988, 0.55042, 0.93159, 0.20326, 0.82204, 0.00006, 0.63906, 0.45903, 0.00023, 0.00038, 0.00224, 0.00002, 0.46599, 0.00010, 0.00005, 0.65340, 0.52709, 0.00069, 0.00064, 0.00001, 0.41938, 0.00022, 0.00003, 0.00038, 0.51716, 0.00050, 0.42688, 0.00023, 0.00025, 0.00054, 0.63455, 0.57043, 0.64877, 0.50914, 0.00036, 0.00081, 0.00008, 0.52317, 0.00043, 0.40138, 0.23428, 0.79580, 0.37499, 0.83582, 0.00014, 0.29250, 0.61545, 0.45807, 0.00002, 0.64465, 0.39064, 0.00003, 0.46637, 0.00016, 0.00056, 0.58329, 0.49980, 0.86303, 0.00001, 0.42345, 0.00025, 0.00064, 0.40050, 0.00003, 0.00015, 0.31782, 0.51847, 0.44327, 0.34165, 0.00054, 0.42409, 0.00030, 0.40706, 0.00039, 0.39849, 0.50376, 0.16808, 0.31401)
fdr <- 0.01
s <- seq(0.01,0.25,0.01)
h0.de <- unlist(lapply(s,function(p) { sum(vals[vals<=p])/sum(vals<=p) }))
exp.thresh <- s[max(which(h0.de<=fdr))] #highest prob threshold below threshold.
yields 0.14
 */
public class ProbThresholdTest {

    private static  List<Double> pvals;
    private static final double desiredFDR = 0.01;
    private static final double DELTA = 0.0001;
    private static PosteriorErrorProbThreshold probThreshold;


    @BeforeAll
    public static void init() {
        double [] vals = {0.00014, 0.94042, 0.73153, 0.36663, 0.32397, 0.00062, 0.55860, 0.58922, 0.00079,
                0.00010, 0.00209, 0.81988, 0.55042, 0.93159, 0.20326, 0.82204, 0.00006, 0.63906, 0.45903, 0.00023,
                0.00038, 0.00224, 0.00002, 0.46599, 0.00010, 0.00005, 0.65340, 0.52709, 0.00069, 0.00064, 0.00001,
                0.41938, 0.00022, 0.00003, 0.00038, 0.51716, 0.00050, 0.42688, 0.00023, 0.00025, 0.00054, 0.63455,
                0.57043, 0.64877, 0.50914, 0.00036, 0.00081, 0.00008, 0.52317, 0.00043, 0.40138, 0.23428, 0.79580,
                0.37499, 0.83582, 0.00014, 0.29250, 0.61545, 0.45807, 0.00002, 0.64465, 0.39064, 0.00003, 0.46637,
                0.00016, 0.00056, 0.58329, 0.49980, 0.86303, 0.00001, 0.42345, 0.00025, 0.00064, 0.40050, 0.00003,
                0.00015, 0.31782, 0.51847, 0.44327, 0.34165, 0.00054, 0.42409, 0.00030, 0.40706, 0.00039, 0.39849,
                0.50376, 0.16808, 0.31401};
        pvals = new ArrayList<>();
        Arrays.stream(vals).forEach(pvals::add);
        probThreshold = new PosteriorErrorProbThreshold(pvals, desiredFDR);
    }


    @Test
    public void testQval() {
        double expectQvalThreshold = 0.23;
        double x = probThreshold.getPepThreshold();
        assertEquals(expectQvalThreshold, probThreshold.getPepThreshold(), DELTA);
    }


    @Test
    public void testSmallExample() {
        double [] vals = {0.0, 0.1, 0.2, 0.25, 0.3};
        pvals = new ArrayList<>();
        Arrays.stream(vals).forEach(pvals::add);
        double fdr_desired = 0.01;
        probThreshold = new PosteriorErrorProbThreshold(pvals, fdr_desired);
        assertEquals(0.1, probThreshold.getPepThreshold(), DELTA);
    }


}
