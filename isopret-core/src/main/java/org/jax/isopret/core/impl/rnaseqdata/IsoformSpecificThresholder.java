package org.jax.isopret.core.impl.rnaseqdata;

import org.jax.isopret.data.AccessionNumber;
import org.jax.isopret.model.GeneResult;
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
 * <p>
 *     The class can also be used for edgeR results, in which case there is a p-value probability threshold
 *     which is usually the standard 0.05, but we can be set as a user-defined parameter.
 * </p>
 * @author Peter N Robinson
 */
public class IsoformSpecificThresholder {
    private static final Logger LOGGER = LoggerFactory.getLogger(IsoformSpecificThresholder.class);

    /** Probability threshold for expression results that attains fdrThreshold FDR for expression. */
    private final double expressionPepThreshold;
    /** Probability threshold for splicing results that attains fdrThreshold FDR for splicing. */
    private final double splicingPepThreshold;

    private final StudySet dgeStudy;
    private final StudySet dgePopulation;
    private final StudySet dasStudy;
    private final StudySet dasPopulation;

    private final Map<AccessionNumber, GeneResult> rawResults;

    private final double fdrThreshold;


    public Map<AccessionNumber, GeneResult> getRawResults() {
        return rawResults;
    }

    public int getTotalGeneCount() {
        return this.rawResults.size();
    }

    public double getFdrThreshold() {
        return fdrThreshold;
    }

    /**
     * This should be used for HBA-DEALS results. The main difference to the factory for edgeR is that
     * we calculated the PEP values for expression and splicing to satisfy the desired FDR.
     * @param results  edgeR RNA-seq results
     * @param fdrThreshold chosen false-discover rate threshold
     * @param geneContainer {@link AssociationContainer} object with GO annotations for expression
     * @param transcriptContainer {@link AssociationContainer} object with GO annotations for isoforms
     * @return an {@link IsoformSpecificThresholder} object
     */
    public static IsoformSpecificThresholder fromHbaDeals(Map<AccessionNumber, GeneResult> results,
                                                   double fdrThreshold,
                                                   AssociationContainer<TermId> geneContainer,
                                                   AssociationContainer<TermId> transcriptContainer) {

        List<Double> expressionProbs = results
                .values()
                .stream()
                .map(GeneResult::getExpressionP)
                .collect(Collectors.toList());
        PosteriorErrorProbThreshold probThresholdExpression = new PosteriorErrorProbThreshold(expressionProbs, fdrThreshold);
        double expressionPepThreshold = probThresholdExpression.getPepThreshold();
        LOGGER.info("Expression PEP threshold {}", expressionPepThreshold);
        List<Double> splicingProbs = results
                .values()
                .stream()
                .map(GeneResult::getSplicingPlist)
                .flatMap(List::stream)
                .collect(Collectors.toList());
        PosteriorErrorProbThreshold probThresholdSplicing = new PosteriorErrorProbThreshold(splicingProbs, fdrThreshold);
        double splicingPepThreshold = probThresholdSplicing.getPepThreshold();
        LOGGER.info("Splicing PEP threshold {}", expressionPepThreshold);
        return new IsoformSpecificThresholder(results,
                fdrThreshold,
                expressionPepThreshold,
                splicingPepThreshold,
                geneContainer,
                transcriptContainer);
    }

    /**
     * This constructor is use for edgeR results. Here, there is just one FDR threshold that is
     * used for both expression and splicing and is usually set to 5%.
     * @param results  edgeR RNA-seq results
     * @param fdrThreshold chosen false-discover rate threshold
     * @param geneContainer {@link AssociationContainer} object with GO annotations for expression
     * @param transcriptContainer {@link AssociationContainer} object with GO annotations for isoforms
     * @return an {@link IsoformSpecificThresholder} object
     */
    public static IsoformSpecificThresholder fromEdgeR(Map<AccessionNumber, GeneResult> results,
                                                   double fdrThreshold,
                                                   AssociationContainer<TermId> geneContainer,
                                                   AssociationContainer<TermId> transcriptContainer) {
        LOGGER.info("edgeR FDR threshold {}", fdrThreshold);
        return new IsoformSpecificThresholder(results,
                fdrThreshold,
                fdrThreshold,
                fdrThreshold,
                geneContainer,
                transcriptContainer);
    }

    /**
     * Find the FDR thresholds for splicing and expression
     * @param results Map of HBA-DEALS analysis results (key: gene symbol)
     */
    private IsoformSpecificThresholder(Map<AccessionNumber, GeneResult> results,
                                      double fdrThreshold,
                                      double expressionThreshold,
                                      double splicingThreshold,
                                      AssociationContainer<TermId> geneContainer,
                                      AssociationContainer<TermId> transcriptContainer) {

        this.rawResults = results;
        this.fdrThreshold = fdrThreshold;
        this.expressionPepThreshold = expressionThreshold;
        this.splicingPepThreshold = splicingThreshold;
        Set<TermId> dgeSignificant = results
                .values()
                .stream()
                .filter(r -> r.getExpressionP() <= this.expressionPepThreshold)
                .map(GeneResult::getGeneAccession)
                .map(AccessionNumber::toTermId)
                .collect(Collectors.toSet());
        Set<TermId> dgePopulation = results
                .values()
                .stream()
                .map(GeneResult::getGeneAccession)
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
                .filter(tr -> tr.getPvalue() <= splicingPepThreshold)
                .map(TranscriptResultImpl::getTranscriptId)
                .map(AccessionNumber::toTermId)
                .collect(Collectors.toSet());
        Set<TermId> dasIsoformPopulation = results
                .values()
                .stream()
                .flatMap(r -> r.getTranscriptResults().stream())
                .map(TranscriptResultImpl::getTranscriptId)
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
