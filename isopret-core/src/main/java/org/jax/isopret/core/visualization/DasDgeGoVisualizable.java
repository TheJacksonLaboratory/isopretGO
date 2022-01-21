package org.jax.isopret.core.visualization;

import org.monarchinitiative.phenol.analysis.stats.GoTerm2PValAndCounts;
import org.monarchinitiative.phenol.ontology.data.TermId;

public class DasDgeGoVisualizable implements Comparable<DasDgeGoVisualizable> {

    private final String goId;
    private final String goLabel;
    private final int dasStudyTotal;
    private final int dasStudyAnnotated;
    private final int dasPopTotal;
    private final int dasPopAnnotated;
    private final double dasP;
    private final double dasAdjP;
    private final int dgeStudyTotal;
    private final int dgeStudyAnnotated;
    private final int dgePopTotal;
    private final int dgePopAnnotated;
    private final double dgeP;
    private final double dgeAdjP;


    public DasDgeGoVisualizable(String goId, String goLabel, int dasStudyTotal, int dasStudyAnnotated, int dasPopTotal, int dasPopAnnotated, double dasP, double dasAdjP, int dgeStudyTotal, int dgeStudyAnnotated, int dgePopTotal, int dgePopAnnotated, double dgeP, double dgeAdjP) {
        this.goId = goId;
        this.goLabel = goLabel;
        this.dasStudyTotal = dasStudyTotal;
        this.dasStudyAnnotated = dasStudyAnnotated;
        this.dasPopTotal = dasPopTotal;
        this.dasPopAnnotated = dasPopAnnotated;
        this.dasP = dasP;
        this.dasAdjP = dasAdjP;
        this.dgeStudyTotal = dgeStudyTotal;
        this.dgeStudyAnnotated = dgeStudyAnnotated;
        this.dgePopTotal = dgePopTotal;
        this.dgePopAnnotated = dgePopAnnotated;
        this.dgeP = dgeP;
        this.dgeAdjP = dgeAdjP;
    }

    public String getGoId() {
        return goId;
    }

    public String getGoLabel() {
        return goLabel;
    }

    public int getDasStudyTotal() {
        return dasStudyTotal;
    }

    public int getDasStudyAnnotated() {
        return dasStudyAnnotated;
    }

    public int getDasPopTotal() {
        return dasPopTotal;
    }

    public int getDasPopAnnotated() {
        return dasPopAnnotated;
    }

    public double getDasP() {
        return dasP;
    }

    public double getDasAdjP() {
        return dasAdjP;
    }

    public int getDgeStudyTotal() {
        return dgeStudyTotal;
    }

    public int getDgeStudyAnnotated() {
        return dgeStudyAnnotated;
    }

    public int getDgePopTotal() {
        return dgePopTotal;
    }

    public int getDgePopAnnotated() {
        return dgePopAnnotated;
    }

    public double getDgeP() {
        return dgeP;
    }

    public double getDgeAdjP() {
        return dgeAdjP;
    }

    public static DasDgeGoVisualizable fromDasDge(TermId goId, String label, GoTerm2PValAndCounts das, GoTerm2PValAndCounts dge) {
        return new DasDgeGoVisualizable(goId.getValue(),
                label,
                das.getTotalStudyGenes(),
                das.getAnnotatedStudyGenes(),
                das.getTotalPopulationGenes(),
                das.getAnnotatedPopulationGenes(),
                das.getRawPValue(),
                das.getAdjustedPValue(),
                dge.getTotalStudyGenes(),
                dge.getAnnotatedStudyGenes(),
                dge.getTotalPopulationGenes(),
                dge.getAnnotatedPopulationGenes(),
                dge.getRawPValue(),
                dge.getAdjustedPValue());
    }

    public static DasDgeGoVisualizable fromDas(TermId goId, String label, GoTerm2PValAndCounts das) {
        return new DasDgeGoVisualizable(goId.getValue(),
                label,
                das.getTotalStudyGenes(),
                das.getAnnotatedStudyGenes(),
                das.getTotalPopulationGenes(),
                das.getAnnotatedPopulationGenes(),
                das.getRawPValue(),
                das.getAdjustedPValue(),
                0,
                0,
                0,
                0,
                1,
                1);
    }


    public static DasDgeGoVisualizable fromDge(TermId goId, String label, GoTerm2PValAndCounts dge) {
        return new DasDgeGoVisualizable(goId.getValue(),
                label,
                0,
                0,
                0,
                0,
                1,
                1,
                dge.getTotalStudyGenes(),
                dge.getAnnotatedStudyGenes(),
                dge.getTotalPopulationGenes(),
                dge.getAnnotatedPopulationGenes(),
                dge.getRawPValue(),
                dge.getAdjustedPValue());
    }

    @Override
    public int compareTo(DasDgeGoVisualizable that) {
        return Double.compare(Math.min(dgeAdjP, dasAdjP),
                Math.min(that.dgeAdjP, that.dasAdjP));
    }
}
