package org.jax.isopret.hbadeals;

import org.jax.isopret.transcript.AccessionNumber;

import java.util.*;
import java.util.stream.Collectors;

public class HbaDealsResult {
    /** Accession number of the gene, e.g., ENSG00000001167. */
    private final String geneAccession;
    /** Integer representing Accession number of the gene, e.g., 1167 for ENSG00000001167. */
    private final int ensemblId;
    private final String symbol;
    private double expressionFoldChange;
    private double expressionP;
    private final Map<String, HbaDealsTranscriptResult> transcriptMap;



    public HbaDealsResult(String geneAccession, String sym) {
        this.geneAccession = geneAccession;
        this.ensemblId = AccessionNumber.ensgAccessionToInt(geneAccession);
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

    public String getGeneAccession() {
        return geneAccession;
    }

    /**
     * @return an int representing Accession number of the gene, e.g., 1167 for ENSG00000001167. */
    public int getEnsgId() {
        return this.ensemblId;
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


    public Map<String, HbaDealsTranscriptResult> getTranscriptMap() {
        return transcriptMap;
    }

    public Map<Integer, HbaDealsTranscriptResult> getTranscriptMap2() {
        Map<Integer, HbaDealsTranscriptResult> transcriptmap = new HashMap<>();
        for (HbaDealsTranscriptResult res : this.transcriptMap.values()) {
            transcriptmap.put(res.getTranscriptId(), res);
        }
        return transcriptmap;
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




    public double getSmallestSplicingP() {
        return this.transcriptMap
                .values()
                .stream()
                .map(HbaDealsTranscriptResult::getP)
                .min(Double::compareTo)
                .orElse(1.0);
    }

}
