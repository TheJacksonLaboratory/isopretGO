package org.jax.isopret.model;

import org.jax.isopret.core.impl.hbadeals.HbaDealsResult;
import org.jax.isopret.core.impl.hbadeals.HbaDealsTranscriptResult;
import org.jax.isopret.core.impl.interpro.DisplayInterproAnnotation;
import org.jax.isopret.core.impl.interpro.InterproEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class AnnotatedGene implements Comparable<AnnotatedGene> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AnnotatedGene.class);
    /** All annotated transcripts of some gene */
    private final List<Transcript> transcripts;

    /** All annotated transcripts of some gene that were expressed according to HBA deals */
    private final List<Transcript> expressedTranscripts;
    /** Key, a transcript object for an expressed transcript. Value -- corresponding log2 fold change. */
    private final Map<Transcript, Double> expressedTranscriptMap;

    /** Key -- accession number of a transcript; value -- corresponding Interpro annotations .*/
    private final Map<AccessionNumber, List<DisplayInterproAnnotation>> transcriptToInterproHitMap;

    private final HbaDealsResult hbaDealsResult;

    private final Boolean differentiallyExpressed;

    private final Boolean differentiallySpliced;

    private final double expressionThreshold;

    private final double splicingThreshold;



    /**
     *
     * @param transcripts transcripts encoded by this gene
     * @param transcriptToInterproHitMap Interpro hits for the transcripts
     * @param result result of HBA-DEALS analysis for this gene.
     * @param expressionThreshold posterior error probability (differential gene expression)
     * @param splicingThreshold  posterior error probability (differential splicing)
     */
    public AnnotatedGene(List<Transcript> transcripts,
                         Map<AccessionNumber, List<DisplayInterproAnnotation>> transcriptToInterproHitMap,
                         HbaDealsResult result,
                         double expressionThreshold,
                         double splicingThreshold) {
        this.transcripts = transcripts;

        this.hbaDealsResult = result;
        // use HBA Deals results to filter for transcripts that are actually expressed
        Map<AccessionNumber, HbaDealsTranscriptResult> transcriptMap = result.getTranscriptMap();
        expressedTranscripts = transcripts
                .stream()
                .filter(t -> transcriptMap.containsKey(t.accessionId()))
                .collect(Collectors.toList());
        // restrict the transcript/interpro map to transcripts that are actually expressed.
        this.transcriptToInterproHitMap = transcriptToInterproHitMap
                .entrySet()
                .stream()
                .filter(e -> result.transcriptExpressed(e.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        this.differentiallyExpressed = result.hasDifferentialExpressionResult(expressionThreshold);
        this.differentiallySpliced = result.hasDifferentialSplicingResult(splicingThreshold);
        this.expressionThreshold = expressionThreshold;
        this.splicingThreshold = splicingThreshold;
        expressedTranscriptMap = new HashMap<>();
        for (Transcript t: transcripts) {
            AccessionNumber accession = t.accessionId();
            if (transcriptMap.containsKey(accession)) {
                double logFC = transcriptMap.get(accession).getLog2FoldChange();
                expressedTranscriptMap.put(t, logFC);
            }
        }
    }


    public List<Transcript> getExpressedTranscripts() {
        return expressedTranscripts;
    }


    public Map<AccessionNumber, List<DisplayInterproAnnotation>> getTranscriptToInterproHitMap() {
        return transcriptToInterproHitMap;
    }

    /**
     * This function counts each interpro domain only once (this is important because
     * some proteins have multiple of the same domain).
     * @return map with key accession number of a transcript, value -- set of interpro's associated with the transcript
     */
    public Map<AccessionNumber, Set<InterproEntry>> getTranscriptToUniqueInterproMap() {
        Map<AccessionNumber, Set<InterproEntry>> uniqCountMap = new HashMap<>();
        Set<Integer> alreadySeen = new HashSet<>();
        for (Map.Entry<AccessionNumber, List<DisplayInterproAnnotation>> entry : transcriptToInterproHitMap.entrySet()) {
            AccessionNumber acc = entry.getKey();
            // Note that DisplayInterproAnnotation can be unique because of different positions
            // for this function, we only want to count any one Interpro Entry once
            Set<InterproEntry> interproSet = entry.getValue()
                            .stream()
                                    .map(DisplayInterproAnnotation::getInterproEntry)
                                            .collect(Collectors.toSet());
            uniqCountMap.put(acc, interproSet);
        }
        return uniqCountMap;

    }

    public int getTranscriptCount() {
        return expressedTranscripts.size();
    }

    public String getSymbol() { return this.hbaDealsResult.getGeneModel().geneSymbol(); }

    public AccessionNumber getGeneAccessionNumber() {
        return hbaDealsResult.getGeneAccession();
    }

    public int getCodingTranscriptCount() {
        return (int) this.expressedTranscripts
                .stream()
                .filter(Transcript::isCoding)
                .count();
    }

    public int getNoncodingTranscriptCount() {
        return (int) this.expressedTranscripts
                .stream()
                .filter(Predicate.not(Transcript::isCoding))
                .count();
    }


    public List<Transcript> getTranscripts() {
        return transcripts;
    }

    public HbaDealsResult getHbaDealsResult() {
        return hbaDealsResult;
    }

    /**
     * If a differential expression threshold was provided, return its value. Otherwise we are not thresholding, return true
     * @return true if this gene is differentially expression
     */
    public boolean passesExpressionThreshold() {
        return this.differentiallyExpressed == null || this.differentiallyExpressed;
    }
    /**
     * If a differential expression threshold was provided, return its value. Otherwise we are not thresholding, return true
     * @return true if this gene is differentially spliced
     */
    public boolean passesSplicingThreshold() {
        return this.differentiallySpliced == null || this.differentiallySpliced;
    }

    public boolean passesSplicingAndExpressionThreshold() {
        return passesExpressionThreshold() && passesSplicingThreshold();
    }

    public double getSplicingThreshold() {
        return this.splicingThreshold;
    }

    public double getExpressionThreshold() { return this.expressionThreshold; }

    public Map<Transcript, Double> getUpregulatedExpressedTranscripts() {
        return this.expressedTranscriptMap
                .entrySet()
                .stream()
                .filter(e -> e.getValue() >= 0)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public Map<Transcript, Double> getDownregulatedExpressedTranscripts() {
        return this.expressedTranscriptMap
                .entrySet()
                .stream()
                .filter(e -> e.getValue() < 0)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public boolean hasInterproAnnotations() {
        for (AccessionNumber transcriptId : this.transcriptToInterproHitMap.keySet()) {
            if (this.hbaDealsResult.getTranscriptMap().containsKey(transcriptId)) {
                return true;
            }
        }
        return false;
    }


    /**
     * We are sort by whether a gene is differentially spliced and then alphabetically
     */
    @SuppressWarnings("NullableProblems")
    @Override
    public int compareTo(AnnotatedGene that) {
        if (that==null) return 0;
        if (this.passesSplicingAndExpressionThreshold() && (!that.passesSplicingAndExpressionThreshold())) {
            return -1;
        }  else if (this.passesSplicingThreshold() && (!that.passesSplicingThreshold())) {
            return -1;
        } else if (that.passesSplicingAndExpressionThreshold() && (!this.passesSplicingAndExpressionThreshold())) {
            return 1;
        } else if (that.passesSplicingThreshold() && (!this.passesSplicingThreshold())) {
            return 1;
        } else {
            return this.getSymbol().compareTo(that.getSymbol());
        }
    }



}
