package org.jax.isopret.core.impl.hbadeals;

import org.jax.isopret.model.TranscriptResult;
import org.jax.isopret.model.AccessionNumber;

public class HbaDealsTranscriptResult implements TranscriptResult {
    private final AccessionNumber transcript;
    private final double foldChange;
    private final double P;


    public HbaDealsTranscriptResult(AccessionNumber transcript, double fc, double p) {
        this.transcript = transcript;
        this.foldChange = fc;
        this.P = p;
    }

    @Override
    public String getTranscript() {
        return transcript.getAccessionString();
    }
    @Override
    public AccessionNumber getTranscriptId() {
        return transcript;
    }
    @Override
    public double getFoldChange() {
        return foldChange;
    }
    @Override
    public double getLog2FoldChange() {
        if (foldChange == 0.0) return foldChange;
        return Math.log(foldChange)/Math.log(2.0);
    }
    @Override
    public double getPvalue() {
        return P;
    }
    @Override
    public boolean isSignificant(double threshold) {
        return this.P < threshold;
    }
}
