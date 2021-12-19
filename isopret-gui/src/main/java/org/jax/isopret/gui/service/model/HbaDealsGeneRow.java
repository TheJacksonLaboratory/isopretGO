package org.jax.isopret.gui.service.model;

import org.jax.isopret.core.hbadeals.HbaDealsResult;
import org.jax.isopret.core.hbadeals.HbaDealsTranscriptResult;

public class HbaDealsGeneRow {


   private final int significantIsoforms;

    private final HbaDealsResult result;

    public HbaDealsGeneRow(HbaDealsResult result) {
        this.result = result;
        this.significantIsoforms = (int)result.getTranscriptMap().values().stream().filter(HbaDealsTranscriptResult::isSignificant).count();
    }
    public String getGeneSymbol() {
        return result.getSymbol();
    }

    public String getGeneAccession() {
        return result.getGeneAccession().getAccessionString();
    }

    public int getSignificantIsoforms() {
        return significantIsoforms;
    }

    public int getTotalIsoforms() {
        return result.getTranscriptMap().size();
    }

    public double getExpressionFoldChange() {
        return result.getExpressionFoldChange();
    }

    public double getExpressionPval() {
        return result.getExpressionP();
    }

    public double getBestSplicingPval() {
        return result.getSmallestSplicingP();
    }

    public String getNofMsplicing() {
        return String.format("%d/%d", this.getSignificantIsoforms(), this.getTotalIsoforms());
    }


}
