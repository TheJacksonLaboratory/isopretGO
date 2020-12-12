package org.jax.isopret.hbadeals;

import java.util.*;

public class HbaDealsResult {
    /** Some genes only have a splicing result. */
    private boolean hasExpressionResult = false;
    /** Accession number of the gene, e.g., ENSG00000001167. */
    private final String geneAccession;
    private final String symbol;
    private double expressionFoldChange;
    private double expressionP;
    private double correctedPval;
    private final Map<String, HbaDealsTranscriptResult> transcriptMap;


    public HbaDealsResult(String geneAccession, String sym) {
        this.geneAccession = geneAccession;
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

    public String getGeneAccession() {
        return geneAccession;
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

    public double getCorrectedPval() {
        return correctedPval;
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
                .anyMatch(HbaDealsTranscriptResult::isSignificant);
    }

    public boolean hasSignificantExpressionResult() {
        return this.correctedPval < 0.05;
    }

    public boolean hasaSignificantSplicingResult() {
        return this.transcriptMap
                .values()
                .stream()
                .anyMatch(HbaDealsTranscriptResult::isSignificant);
    }

    public double getMostSignificantSplicingPval() {
        return this.transcriptMap
                .values()
                .stream()
                .map(HbaDealsTranscriptResult::getCorrectedP)
                .min(Double::compareTo)
                .orElse(1.0);
    }
}
