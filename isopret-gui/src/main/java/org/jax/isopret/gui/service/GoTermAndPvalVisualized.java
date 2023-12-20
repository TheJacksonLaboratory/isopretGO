package org.jax.isopret.gui.service;

import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.monarchinitiative.phenol.analysis.stats.GoTerm2PValAndCounts;

import java.util.Optional;

public class GoTermAndPvalVisualized {

    private final int annotatedStudyGenes;
    private final int annotatedPopulationGenes;
    private final int totalStudyGenes;
    private final int totalPopulationGenes;

    private final TermId goTermId;
    private final String goTermLabel;
    private final double p_raw;
    private final double p_adjusted;


    public GoTermAndPvalVisualized(GoTerm2PValAndCounts pval, Ontology ontology) {
        this.annotatedStudyGenes = pval.getAnnotatedStudyGenes();
        this.totalStudyGenes = pval.getTotalStudyGenes();
        this.annotatedPopulationGenes = pval.getAnnotatedPopulationGenes();
        this.totalPopulationGenes = pval.getTotalPopulationGenes();
        this.goTermId = pval.getItem();
        this.p_raw = pval.getRawPValue();
        this.p_adjusted = pval.getAdjustedPValue();
        Optional<String> labelOpt = ontology.getTermLabel(goTermId);
        this.goTermLabel = labelOpt.orElse("n/a");
    }

    public int getAnnotatedStudyGenes() {
        return annotatedStudyGenes;
    }

    public int getAnnotatedPopulationGenes() {
        return annotatedPopulationGenes;
    }

    public int getTotalStudyGenes() {
        return totalStudyGenes;
    }

    public String getStudyGeneRatio() {
        return String.format("%d/%d (%.1f%%)", this.annotatedStudyGenes, this.totalStudyGenes, 100.0 * this.annotatedStudyGenes / this.totalStudyGenes);
    }


    public String getPopulationGeneRatio() {
        return String.format("%d/%d (%.1f%%)", this.annotatedPopulationGenes, this.totalPopulationGenes, 100.0 * this.annotatedPopulationGenes / this.totalPopulationGenes);
    }

    public int getTotalPopulationGenes() {
        return totalPopulationGenes;
    }

    public String getGoTermId() {
        return goTermId.getValue();
    }

    public String getGoTermLabel() {
        return goTermLabel;
    }

    public double getP_raw() {
        return p_raw;
    }

    public String getPvalFormated() {
        return formatP(p_raw);
    }

    public String getPvalAdjFormated() {
        return formatP(p_adjusted);
    }

    private String formatP(double p) {
        if (p > 0.05) {
            return String.format("%.2f", p);
        } else if (p > 0.001) {
            return String.format("%.3f", p);
        } else {
            return String.format("%e", p);
        }
    }

    public double getP_adjusted() {
        return p_adjusted;
    }






}
