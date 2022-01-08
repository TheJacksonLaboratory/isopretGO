package org.jax.isopret.gui.service.model;

import org.monarchinitiative.phenol.ontology.data.TermId;

/**
 * Convenience class to help visualize GO terms in the GUI
 * {@link #dge} is the negative decadic logarithm of the p-value of
 * enrichment for the differential gene expression dataset and
 * {@link #das} is the same for the differential alternative splicing dataset.
 * @author Peter Robinson
 */
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
     * @return true if this term is signficantly overrepresented for gene expression
     */
    public boolean dgeSignificant() {
        return this.dge >= THRESH;
    }
    /**
     * p-values are -log10
     * @return true if this term is signficantly overrepresented for alternative splicing
     */
    public boolean dasSignificant() {
        return this.das >= THRESH;
    }

    public boolean isSignificant() { return this.dasSignificant() || this.dgeSignificant(); }

    public boolean dgePredominant() { return this.dge >= this.das; }
    public boolean dasPredominant() { return this.das > this.dge; }
}
