package org.jax.prositometry.hbadeals;

public class HbaDealsTranscriptResult {
    private final String transcript;
    private final double foldChange;
    private final double P;
    private final double correctedP;

    public HbaDealsTranscriptResult(String transcript, double fc, double p, double correctedP) {
        this.transcript = transcript;
        this.foldChange = fc;
        this.P = p;
        this.correctedP = correctedP;
    }


    public String getTranscript() {
        return transcript;
    }

    public double getFoldChange() {
        return foldChange;
    }

    public double getP() {
        return P;
    }

    public double getCorrectedP() {
        return correctedP;
    }

    public boolean isSignificant() {
        return correctedP <= 0.05;
    }
}
