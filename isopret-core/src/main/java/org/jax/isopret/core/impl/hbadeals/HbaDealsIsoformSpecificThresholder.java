package org.jax.isopret.core.impl.hbadeals;

import org.jax.isopret.model.AccessionNumber;
import org.monarchinitiative.phenol.analysis.AssociationContainer;
import org.monarchinitiative.phenol.analysis.StudySet;
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
 */
public class HbaDealsIsoformSpecificThresholder {
    private static final Logger LOGGER = LoggerFactory.getLogger(HbaDealsIsoformSpecificThresholder.class);

    private static final double DEFAULT_THRESHOLD = 0.05;

    /** Probability threshold for expression results that attains fdrThreshold FDR for expression. */
    private final double expressionPepThreshold;
    /** Probability threshold for splicing results that attains fdrThreshold FDR for splicing. */
    private final double splicingPepThreshold;

    private final StudySet dgeStudy;
    private final StudySet dgePopulation;
    private final StudySet dasStudy;
    private final StudySet dasPopulation;

    private final Map<AccessionNumber, HbaDealsResult> rawResults;

    private final double fdrThreshold;


    public Map<AccessionNumber, HbaDealsResult> getRawResults() {
        return rawResults;
    }

    public int getTotalGeneCount() {
        return this.rawResults.size();
    }

    public double getFdrThreshold() {
        return fdrThreshold;
    }

    /**
     * Find the FDR thresholds for splicing and expression
     * @param results Map of HBA-DEALS analysis results (key: gene symbol)
     */
    public HbaDealsIsoformSpecificThresholder(Map<AccessionNumber, HbaDealsResult> results,
                                              double fdrThreshold,
                                              AssociationContainer<TermId> geneContainer,
                                              AssociationContainer<TermId> transcriptContainer) {

        this.rawResults = results;
        this.fdrThreshold = fdrThreshold;
        List<Double> expressionProbs = results
                .values()
                .stream()
                .map(HbaDealsResult::getExpressionP)
                .collect(Collectors.toList());
        PosteriorErrorProbThreshold probThresholdExpression = new PosteriorErrorProbThreshold(expressionProbs, fdrThreshold);
        this.expressionPepThreshold = probThresholdExpression.getPepThreshold();
        LOGGER.info("Expression PEP threshold {}", this.expressionPepThreshold);
        List<Double> splicingProbs = results
                .values()
                .stream()
                .map(HbaDealsResult::getSplicingPlist)
                .flatMap(List::stream)
                .collect(Collectors.toList());
        PosteriorErrorProbThreshold probThresholdSplicing = new PosteriorErrorProbThreshold(splicingProbs, fdrThreshold);
        this.splicingPepThreshold = probThresholdSplicing.getPepThreshold();
        LOGGER.info("Splicing PEP threshold {}", this.expressionPepThreshold);
        Set<TermId> dgeSignificant = results
                .values()
                .stream()
                .filter(r -> r.getExpressionP() <= this.expressionPepThreshold)
                .map(HbaDealsResult::getGeneAccession)
                .map(AccessionNumber::toTermId)
                .collect(Collectors.toSet());
        Set<TermId> dgePopulation = results
                .values()
                .stream()
                .map(HbaDealsResult::getGeneAccession)
                .map(AccessionNumber::toTermId)
                .collect(Collectors.toSet());
        LOGGER.info("DGE: {} study set and {} population genes", dgeSignificant.size(), dgePopulation.size());
        var assocMap = geneContainer.getAssociationMap(dgeSignificant);
        this.dgeStudy = new StudySet("DGE Study", assocMap);
        assocMap = geneContainer.getAssociationMap(dgePopulation);
        this.dgePopulation = new StudySet("DGE Population", assocMap);

        Set<TermId> dasIsoformStudy = results
                .values()
                .stream()
                .flatMap(r -> r.getTranscriptResults().stream())
                .filter(tr -> tr.getP() <= splicingPepThreshold)
                .map(HbaDealsTranscriptResult::getTranscriptId)
                .map(AccessionNumber::toTermId)
                .collect(Collectors.toSet());
        Set<TermId> dasIsoformPopulation = results
                .values()
                .stream()
                .flatMap(r -> r.getTranscriptResults().stream())
                .map(HbaDealsTranscriptResult::getTranscriptId)
                .map(AccessionNumber::toTermId)
                .collect(Collectors.toSet());
        LOGGER.info("DAS: {} study set and {} population genes", dasIsoformStudy.size(), dasIsoformPopulation.size());
        assocMap = transcriptContainer.getAssociationMap(dasIsoformStudy);
        this.dasStudy = new StudySet("DAS Study", assocMap);
        LOGGER.info("DAS: study set {} annotated items",dasStudy.getAnnotatedItemCount());
        assocMap = transcriptContainer.getAssociationMap(dasIsoformPopulation);
        this.dasPopulation = new StudySet("DAS Population", assocMap);
        LOGGER.info("DAS: Population set {} annotated items",dasPopulation.getAnnotatedItemCount());
    }


    public StudySet getDgeStudy() {
        return this.dgeStudy;
    }

    public int getDgeGeneCount() {
        return this.dgeStudy.getAnnotatedItemCount();
    }

    public StudySet getDgePopulation() {
        return this.dgePopulation;
    }



    public StudySet getDasStudy() {
        return this.dasStudy;
    }

    public int getDasIsoformCount() {
        return this.dasStudy.getAnnotatedItemCount();
    }

    public StudySet getDasPopulation() {
        return this.dasPopulation;
    }


    public double getExpressionPepThreshold() {
        return expressionPepThreshold;
    }

    public double getSplicingPepThreshold() {
        return splicingPepThreshold;
    }


}
