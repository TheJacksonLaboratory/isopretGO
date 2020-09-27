package org.jax.prositometry.hbadeals;

import java.util.HashMap;
import java.util.Map;

public class HbaDealsResult {
    private static final double UNINITIALIZED = -1.0;
    private final String symbol;
    private double expressionFoldChange = UNINITIALIZED;
    private double expressionP = UNINITIALIZED;
    private Map<String, HbaDealsTranscriptResult> transcriptMap;
    public HbaDealsResult(String sym) {
        this.symbol = sym;
        transcriptMap = new HashMap<>();
    }

    public void addExpressionResult(double fc, double p) {
        this.expressionFoldChange = fc;
        this.expressionP = p;
    }

    public void addTranscriptResult(String isoform, double expFC, double P) {
        int i = isoform.indexOf(".");
        if (i>0) {
            // remove version
            isoform = isoform.substring(0,i);
        }
        HbaDealsTranscriptResult tresult = new HbaDealsTranscriptResult(isoform, expFC, P);
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
}
