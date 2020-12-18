package org.jax.isopret.hbadeals;

public class HbaDealsTranscriptResult {
    private final String transcript;
    private final double foldChange;
    private final double P;


    public HbaDealsTranscriptResult(String transcript, double fc, double p) {
        this.transcript = transcript;
        this.foldChange = fc;
        this.P = p;
    }


    public String getTranscript() {
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

    public boolean isSignificant() {
        return P <= 0.05;
    }
}
