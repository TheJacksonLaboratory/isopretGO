package org.jax.prositometry.hbadeals;

public class HbaDealsGeneResult {

    private final String gene;
    private final String transcript;
    private final double foldChange;
    private final double P;

    HbaDealsGeneResult(String gene, String transcript, double fc, double p) {
        this.gene = gene;
        this.transcript = transcript;
        this.foldChange = fc;
        this.P = p;
    }

    public String getGene() {
        return gene;
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
}
