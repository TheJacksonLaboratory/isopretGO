package org.jax.isopret.hbadeals;

import org.jax.isopret.transcript.AccessionNumber;

import java.util.*;
import java.util.stream.Collectors;

public class HbaDealsResult {
    /** Accession number of the gene, e.g., ENSG00000001167. */
    private final AccessionNumber geneAccession;
    private final String symbol;
    private double expressionFoldChange;
    private double expressionP;
    private final Map<AccessionNumber, HbaDealsTranscriptResult> transcriptMap;



    public HbaDealsResult(AccessionNumber geneAccession, String sym) {
        this.geneAccession = geneAccession;
        this.symbol = sym;
        transcriptMap = new HashMap<>();
    }

    public void addExpressionResult(double fc, double p) {
        this.expressionFoldChange = fc;
        this.expressionP = p;
    }

    public void addTranscriptResult(AccessionNumber isoform, double expFC, double P) {
        HbaDealsTranscriptResult tresult = new HbaDealsTranscriptResult(isoform, expFC, P);
        transcriptMap.putIfAbsent(isoform, tresult);
    }

    public AccessionNumber getGeneAccession() {
        return geneAccession;
    }

    /**
     * @return an int representing Accession number of the gene, e.g., 1167 for ENSG00000001167. */
    public int getEnsgId() {
        return this.geneAccession.getAccessionNumber();
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

    public List<Double> getSplicingPlist() {
        return this.transcriptMap
                .values()
                .stream()
                .map(HbaDealsTranscriptResult::getP)
                .collect(Collectors.toList());
    }


    public Map<AccessionNumber, HbaDealsTranscriptResult> getTranscriptMap() {
        return transcriptMap;
    }

    public boolean hasDifferentialExpressionResult(double threshold) { return this.expressionP < threshold; }


    public boolean hasDifferentialSplicingResult(double threshold) {
        return this.transcriptMap.values().stream().anyMatch(r -> r.getP() <= threshold);
    }

    /**
     *
     * @param splicing adjusted probability threshold
     * @param expression adjusted probability threshold
     * @return true if this gene has a differential expression OR splicing result
     */
    public boolean hasDifferentialSplicingOrExpressionResult(double splicing, double expression) {
        return hasDifferentialSplicingResult(splicing) || hasDifferentialExpressionResult(expression);
    }

    public boolean transcriptExpressed(AccessionNumber acc) {
        return this.transcriptMap.containsKey(acc);
    }


    public double getSmallestSplicingP() {
        return this.transcriptMap
                .values()
                .stream()
                .map(HbaDealsTranscriptResult::getP)
                .min(Double::compareTo)
                .orElse(1.0);
    }

}
