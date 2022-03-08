package org.jax.isopret.gui.service;

public record InterproOverrepResult(String interproAccession,
                             String interproDescription,
                             int populationTotal,
                             int populationAnnotated,
                             int studyTotal,
                             int studyAnnotated,
                             double rawPval) implements Comparable<InterproOverrepResult> {

    @Override
    public int compareTo(InterproOverrepResult that) {
        return Double.compare(this.rawPval, that.rawPval);
    }
}
