package org.jax.prositometry.html;

public class HtmlTranscript {

    private static final double UNINITIALIZED = -1.0;

    private final String identifier;
    private final String motifs;
    private final String differences;
    private final boolean hasDiff;
    private final int cDNAlength;
    private final int peptideLength;
    private final double pval;
    private final double pvalcorr;

    public HtmlTranscript(String id, String motifs, String differences, boolean hasDiff, int cDNAlen, int aalen, double p, double pcorr) {
        this.identifier = id;
        this.motifs = motifs;
        this.differences = differences;
        this.hasDiff = hasDiff;
        this.cDNAlength = cDNAlen;
        this.peptideLength = aalen;
        this.pval = p;
        this.pvalcorr = pcorr;
        System.out.println("pcorr=" + pcorr);
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getMotifs() {
        return motifs;
    }

    public String getDifferences() {
        return differences;
    }

    public boolean getHasdiff() {
        return hasDiff;
    }

    public int getCdnalength() {
        return cDNAlength;
    }

    public int getPeptidelength() {
        return peptideLength;
    }

    public boolean getHaspval() {
        return pval > UNINITIALIZED;
    }

    public double getPval() {
        return pval;
    }

    public double getPvalcorr() {
        return pvalcorr;
    }
}
