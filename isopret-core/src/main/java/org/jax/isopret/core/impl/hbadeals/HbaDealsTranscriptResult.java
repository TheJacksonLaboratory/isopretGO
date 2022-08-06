package org.jax.isopret.core.impl.hbadeals;

import org.jax.isopret.model.AccessionNumber;

public class HbaDealsTranscriptResult {
    private final AccessionNumber transcript;
    private final double foldChange;
    private final double P;


    public HbaDealsTranscriptResult(AccessionNumber transcript, double fc, double p) {
        this.transcript = transcript;
        this.foldChange = fc;
        this.P = p;
    }


    public String getTranscript() {
        return transcript.getAccessionString();
    }

    public AccessionNumber getTranscriptId() {
        return transcript;
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

    public boolean isSignificant(double threshold) {
        return this.P < threshold;
    }
}
