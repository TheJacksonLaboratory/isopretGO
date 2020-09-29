package org.jax.prositometry.hbadeals;

import java.util.*;

public class HbaDealsResult {
    /** Some genes only have a splicing result. */
    private boolean hasExpressionResult = false;
    private final String symbol;
    private double expressionFoldChange;
    private double expressionP;
    private double correctedPval;
    private Map<String, HbaDealsTranscriptResult> transcriptMap;
    public HbaDealsResult(String sym) {
        this.symbol = sym;
        transcriptMap = new HashMap<>();
    }

    public void addExpressionResult(double fc, double p, double corrP) {
        this.expressionFoldChange = fc;
        this.expressionP = p;
        this.correctedPval = corrP;
        hasExpressionResult = true;
    }

    public void addTranscriptResult(String isoform, double expFC, double P, double corrP) {
        int i = isoform.indexOf(".");
        if (i>0) {
            // remove version
            isoform = isoform.substring(0,i);
        }
        HbaDealsTranscriptResult tresult = new HbaDealsTranscriptResult(isoform, expFC, P, corrP);
        transcriptMap.putIfAbsent(isoform, tresult);
    }

    public String getSymbol() {
        return symbol;
    }

    public double getExpressionFoldChange() {
        return expressionFoldChange;
    }

    public double getExpressionP() {
        return expressionP;
    }

    public Map<String, HbaDealsTranscriptResult> getTranscriptMap() {
        return transcriptMap;
    }

    public boolean hasSignificantResult() {
        if (this.correctedPval <= 0.05) {
            return true;
        }
        return this.transcriptMap
                .values()
                .stream()
                .filter(HbaDealsTranscriptResult::isSignificant)
                .findAny()
                .isPresent();
    }
}
