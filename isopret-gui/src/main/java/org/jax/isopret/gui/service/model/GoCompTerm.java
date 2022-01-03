package org.jax.isopret.gui.service.model;

import org.monarchinitiative.phenol.ontology.data.TermId;

public class GoCompTerm implements Comparable<GoCompTerm> {

    private final TermId tid;
    private final String label;
    private final double dge;
    private final double das;

    private static final double THRESH = -1 * Math.log10(0.05);


    GoCompTerm(TermId tid, String label, double dge, double das) {
        this.tid = tid;
        this.label = label;
        this.dge = dge;
        this.das = das;
    }

    @Override
    public int compareTo(GoCompTerm that) {
        return Double.compare(Math.max(this.dge, this.das), Math.max(that.dge, that.das));
    }

    public TermId getTid() {
        return tid;
    }

    public String getLabel() {
        return label;
    }

    public double getDge() {
        return dge;
    }

    public double getDas() {
        return das;
    }

    /**
     * p-values are -log10
     * @return
     */
    public boolean dgeSignificant() {
        return this.dge >= THRESH;
    }

    public boolean dasSignificant() {
        return this.das >= THRESH;
    }
}
