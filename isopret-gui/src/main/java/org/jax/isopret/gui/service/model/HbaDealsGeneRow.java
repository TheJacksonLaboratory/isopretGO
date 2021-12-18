package org.jax.isopret.gui.service.model;

import org.jax.isopret.core.hbadeals.HbaDealsResult;
import org.jax.isopret.core.hbadeals.HbaDealsTranscriptResult;

public class HbaDealsGeneRow {

    private final String geneSymbol;
    private final String geneAccession;

   private final int significantIsoforms;

    private final int totalIsoforms;

    private final double expressionFoldChange;

    private final double expressionPval;

    private final double bestSplicingPval;


    public String getGeneSymbol() {
        return geneSymbol;
    }

    public String getGeneAccession() {
        return geneAccession;
    }

    public int getSignificantIsoforms() {
        return significantIsoforms;
    }

    public int getTotalIsoforms() {
        return totalIsoforms;
    }

    public double getExpressionFoldChange() {
        return expressionFoldChange;
    }

    public double getExpressionPval() {
        return expressionPval;
    }

    public double getBestSplicingPval() {
        return bestSplicingPval;
    }

    public HbaDealsGeneRow(HbaDealsResult result) {
        this.geneSymbol = result.getSymbol();
        this.geneAccession = result.getGeneAccession().getAccessionString();
        this.totalIsoforms = result.getTranscriptMap().size();
        this.significantIsoforms = (int)result.getTranscriptMap().values().stream().filter(HbaDealsTranscriptResult::isSignificant).count();
        this.expressionFoldChange = result.getExpressionFoldChange();
        this.expressionPval = result.getExpressionP();
        this.bestSplicingPval = result.getSmallestSplicingP();
    }
}
