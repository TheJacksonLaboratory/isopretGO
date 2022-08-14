package org.jax.isopret.core.impl.rnaseqdata;

import org.jax.isopret.model.AccessionNumber;
import org.jax.isopret.model.GeneModel;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * THis class is used to perform Bayesian false discovery rate control.
 * The HBA-DEALS probability resulted for each gene expression assessment (or splicing) is
 * a Posterior Error Probability (PEP). By the linearity of expected value we
 * can add these probabilities at any threshold in order to get the total
 * expected number of false discoveries. If we thus rank the observations by PEP (from smallest to largest)
 * and choose the rank just before the rank where the cumulative mean of the FDR (called qvalue after John Storey),
 * this is where we set the threshold.
 * @author Peter N Robinson
 */
public class HbaDealsThresholder {
    private static final Logger LOGGER = LoggerFactory.getLogger(HbaDealsThresholder.class);

    private static final double DEFAULT_THRESHOLD = 0.05;
    /** Threshold for total probability to calculate Bayesian FDR (Usually, we will use 0.05). */
    private final double fdrThreshold;
    /** HBA-DEALS results for all genes in the experiment. */
    private final Map<AccessionNumber, GeneResultImpl> rawResults;
    /** Probability threshold for expression results that attains {@link #fdrThreshold} FDR for expression. */
    private final double expressionThreshold;
    /** Probability threshold for splicing results that attains {@link #fdrThreshold} FDR for splicing. */
    private final double splicingThreshold;

    private final Set<String> dgeGeneSymbols;

    private final Set<String> dasGeneSymbols;

    /**
     * Find the FDR thresholds for splicing and expression
     * @param results Map of HBA-DEALS analysis results (key: gene symbol)
     */
    public HbaDealsThresholder(Map<AccessionNumber, GeneResultImpl> results) {
       this(results, DEFAULT_THRESHOLD);
    }

    /**
     * Find the FDR thresholds for splicing and expression
     * @param results Map of HBA-DEALS analysis results (key: gene symbol)
     */
    public HbaDealsThresholder(Map<AccessionNumber, GeneResultImpl> results, double fdrThres) {
        rawResults = results;
        fdrThreshold = fdrThres;
        this.expressionThreshold = calculateExpressionThreshold();
        this.splicingThreshold = calculateSplicingThreshold();
        LOGGER.info("Calculated expression PEP threshold as {}, and splicing PEP threshold as {}.",
                expressionThreshold, splicingThreshold);
        this.dgeGeneSymbols = this.rawResults
                .values()
                .stream()
                .filter(r -> r.getExpressionP() <= this.expressionThreshold)
                .map(GeneResultImpl::getGeneModel)
                .map(GeneModel::geneSymbol)
                .collect(Collectors.toSet());
        this.dasGeneSymbols = this.rawResults
                .values()
                .stream()
                .filter(r -> r.getSmallestSplicingP() <= this.splicingThreshold)
                .map(GeneResultImpl::getGeneModel)
                .map(GeneModel::geneSymbol)
                .collect(Collectors.toSet());
        LOGGER.info("Found {} passing genes and {} passing isoforms.",
                dgeGeneSymbols.size(), dasGeneSymbols.size());
    }

    /**
     * @return the probability threshold associated with the q-value threshold to reach the desired FDR
     */
    private double getThreshold(List<Double> probs) {
       PosteriorErrorProbThreshold pthresh = new PosteriorErrorProbThreshold(probs, this.fdrThreshold);
       double pepThresh = pthresh.getPepThreshold();
       return pepThresh;
    }


    private double calculateExpressionThreshold() {
        List<Double> expressionProbs = rawResults
                .values()
                .stream()
                .map(GeneResultImpl::getExpressionP)
                .toList();
       return getThreshold(expressionProbs);
    }

    private double calculateSplicingThreshold() {
        List<Double> splicingProbs = rawResults
                .values()
                .stream()
                .map(GeneResultImpl::getSplicingPlist)
                .flatMap(List::stream)
                .collect(Collectors.toList());
        return getThreshold(splicingProbs);
    }

    public int getTotalGeneCount() {
        return this.rawResults.size();
    }

    public Set<String> dgeGeneSymbols() {
        return this.dgeGeneSymbols;
    }

    public Set<String> dasGeneSymbols() {
        return this.dasGeneSymbols;
    }

    public int getDgeGeneCount() {
        return this.dgeGeneSymbols.size();
    }

    public int getDasGeneCount() {
        return this.dasGeneSymbols.size();
    }



    public Set<AccessionNumber> population() {
        return this.rawResults.keySet();
    }

    public double getFdrThreshold() {
        return fdrThreshold;
    }

    public double getExpressionThreshold() {
        return expressionThreshold;
    }

    public double getSplicingThreshold() {
        return splicingThreshold;
    }

    public Map<AccessionNumber, GeneResultImpl> getRawResults() {
        return rawResults;
    }

    /**
     * @return all the ensembl gene Ids observed in our experiment, regardless of significant differential expr.
     */
    public Set<TermId> getAllGeneTermIds() {
        return rawResults.values().stream()
                .map(GeneResultImpl::getEnsgId)
                .map(AccessionNumber::ensgFromInt)
                .map(AccessionNumber::toTermId)
                .collect(Collectors.toSet());
    }

    /**
     * @return all the ensembl transcript Ids observed in our experiment, regardless of significant differential expr.
     */
    public Set<TermId> getAllTranscriptTermIds() {
        return rawResults.values().stream()
                .map(GeneResultImpl::getTranscriptMap)
                .map(Map::values)
                .flatMap(Collection::stream)
                .map(TranscriptResultImpl::getTranscriptId)
                .map(AccessionNumber::toTermId)
                .collect(Collectors.toSet());
    }

    public Set<TermId> dgeGeneTermIds() {
        return this.rawResults
                .values()
                .stream()
                .filter(r -> r.getExpressionP() <= this.expressionThreshold)
                .map(GeneResultImpl::getGeneAccession)
                .map(AccessionNumber::toTermId)
                .collect(Collectors.toSet());
    }

    public Set<TermId> dasIsoformTermIds() {
        return rawResults.values().stream()
                .map(GeneResultImpl::getTranscriptMap)
                .map(Map::values)
                .flatMap(Collection::stream)
                .filter(r -> r.isSignificant(this.splicingThreshold ))
               .map(TranscriptResultImpl::getTranscriptId)
                .map(AccessionNumber::toTermId)
                .collect(Collectors.toSet());
    }
}
