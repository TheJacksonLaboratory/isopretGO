package org.jax.prositometry.html;

public class HtmlTranscript {

    private final String identifier;
    private final String motifs;
    private final String differences;
    private final boolean hasDiff;
    private final int cDNAlength;
    private final int peptideLength;

    public HtmlTranscript(String id, String motifs, String differences, boolean hasDiff, int cDNAlen, int aalen) {
        this.identifier = id;
        this.motifs = motifs;
        this.differences = differences;
        this.hasDiff = hasDiff;
        this.cDNAlength = cDNAlen;
        this.peptideLength = aalen;
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
}
