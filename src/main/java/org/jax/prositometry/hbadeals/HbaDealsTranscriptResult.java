package org.jax.prositometry.hbadeals;

public class HbaDealsTranscriptResult {
    private final String transcript;
    private final double foldChange;
    private final double P;

    public HbaDealsTranscriptResult(String transcript, double fc, double p) {
        this.transcript = transcript;
        this.foldChange = fc;
        this.P = p;
    }
}
