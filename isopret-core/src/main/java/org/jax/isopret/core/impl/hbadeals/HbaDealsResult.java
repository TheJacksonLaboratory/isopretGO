package org.jax.isopret.core.impl.hbadeals;

import org.jax.isopret.model.AccessionNumber;
import org.jax.isopret.model.GeneModel;
import org.jax.isopret.model.GeneResult;
import org.jax.isopret.model.GeneSymbolAccession;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This class encapsulates the results of HBADEALS for one gene and all of the isoforms of that gene that were
 */
public class HbaDealsResult implements GeneResult, Comparable<HbaDealsResult> {
    /** Accession number of the gene, e.g., ENSG00000001167. */
    private final AccessionNumber geneAccession;
    private final GeneModel geneModel;
    private double expressionFoldChange;
    private double expressionP;
    private final Map<AccessionNumber, HbaDealsTranscriptResult> transcriptMap;



    public HbaDealsResult(AccessionNumber geneAccession, GeneModel sym) {
        this.geneAccession = geneAccession;
        this.geneModel = sym;
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
    @Override
    public AccessionNumber getGeneAccession() {
        return geneAccession;
    }

    /**
     * @return an int representing Accession number of the gene, e.g., 1167 for ENSG00000001167. */
    @Override
    public int getEnsgId() {
        return this.geneAccession.getAccessionNumber();
    }
    @Override
    public GeneModel getGeneModel() {
        return geneModel;
    }
    @Override
    public GeneSymbolAccession getGeneSymbolAccession() {
        return new GeneSymbolAccession(geneModel.geneSymbol(), geneAccession);
    }
    @Override
    public double getExpressionFoldChange() {
        return expressionFoldChange;
    }
    @Override
    public double getExpressionP() {
        return expressionP;
    }
    @Override
    public List<Double> getSplicingPlist() {
        return this.transcriptMap
                .values()
                .stream()
                .map(HbaDealsTranscriptResult::getPvalue)
                .collect(Collectors.toList());
    }

    @Override
    public Map<AccessionNumber, HbaDealsTranscriptResult> getTranscriptMap() {
        return Collections.unmodifiableMap(transcriptMap);
    }

    /**
     * Only expressed transcripts are added to the HBA-DEALS results file.
     * @return Number of expressed transcripts observed for this gene.
     */
    @Override
    public int getExpressedTranscriptCount() {
        return transcriptMap.size();
    }
    @Override
    public int getSignificantTranscriptCount(double pepThreshold) {
        return (int) this.transcriptMap.values().stream().filter(r -> r.getPvalue() <= pepThreshold).count();
    }
    @Override
    public boolean hasDifferentialExpressionResult(double threshold) { return this.expressionP < threshold; }

    @Override
    public boolean hasDifferentialSplicingResult(double threshold) {
        return this.transcriptMap.values().stream().anyMatch(r -> r.getPvalue() <= threshold);
    }

    /**
     *
     * @param splicing adjusted probability threshold
     * @param expression adjusted probability threshold
     * @return true if this gene has a differential expression OR splicing result
     */
    @Override
    public boolean hasDifferentialSplicingOrExpressionResult(double splicing, double expression) {
        return hasDifferentialSplicingResult(splicing) || hasDifferentialExpressionResult(expression);
    }
    @Override
    public boolean transcriptExpressed(AccessionNumber acc) {
        return this.transcriptMap.containsKey(acc);
    }

    @Override
    public double getSmallestSplicingP() {
        return this.transcriptMap
                .values()
                .stream()
                .map(HbaDealsTranscriptResult::getPvalue)
                .min(Double::compareTo)
                .orElse(1.0);
    }

    private double minp() {
        return Math.min(getExpressionP(), getSmallestSplicingP());
    }
    @Override
    public Set<HbaDealsTranscriptResult> getTranscriptResults() {
        return new HashSet<>(this.transcriptMap.values());
    }

    /**
     * Sort according to the smallest ('most significant') p value for expression or splicing.
     */
    @Override
    public int compareTo(HbaDealsResult that) {
        return Double.compare(this.minp(), that.minp());
    }
}
