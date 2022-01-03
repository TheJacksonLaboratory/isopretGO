package org.jax.isopret.gui.service.model;

import org.monarchinitiative.phenol.ontology.data.TermId;

public class GoCompTerm implements Comparable<GoCompTerm> {

    private final TermId tid;
    private final String label;
    private final double dge;
    private final double das;


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
}
