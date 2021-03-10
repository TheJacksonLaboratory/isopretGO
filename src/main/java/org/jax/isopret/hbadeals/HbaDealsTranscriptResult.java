package org.jax.isopret.hbadeals;

import org.jax.isopret.transcript.AccessionNumber;

public class HbaDealsTranscriptResult {
    private final AccessionNumber transcript;
    private final double foldChange;
    private final double P;

    private final double DEFAULT_THRESHOLD = 0.13;


    public HbaDealsTranscriptResult(AccessionNumber transcript, double fc, double p) {
        this.transcript = transcript;
        this.foldChange = fc;
        this.P = p;
    }


    public String getTranscript() {
        return transcript.getAccessionString();
    }

    public int getTranscriptId() {
        return transcript.getAccessionNumber();
    }

    public double getFoldChange() {
        return foldChange;
    }

    public double getLog2FoldChange() {
        if (foldChange == 0.0) return foldChange;
        return Math.log(foldChange)/Math.log(2.0);
    }

    public double getP() {
        return P;
    }

    public boolean isSignificant() {
        return isSignificant(DEFAULT_THRESHOLD);
    }

    public boolean isSignificant(double threshold) {
        return this.P < threshold;
    }
}
