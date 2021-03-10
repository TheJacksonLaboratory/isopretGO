package org.jax.isopret.transcript;

import org.jax.isopret.hbadeals.HbaDealsResult;
import org.jax.isopret.hbadeals.HbaDealsTranscriptResult;
import org.jax.isopret.interpro.DisplayInterproAnnotation;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class AnnotatedGene implements Comparable<AnnotatedGene> {
    /** All annotated transcripts of some gene */
    private final List<Transcript> transcripts;

    /** All annotated transcripts of some gene that were expressed according to HBA deals */
    private final List<Transcript> expressedTranscripts;
    /** Key, a transcript object for an expressed transcript. Value -- corresponding log2 fold change. */
    private final Map<Transcript, Double> expressedTranscriptMap;

    /** Key -- accession number of a transcript; value -- corresponding Interpro annotations .*/
    private final Map<AccessionNumber, List<DisplayInterproAnnotation>> transcriptToInterproHitMap;

    private final HbaDealsResult hbaDealsResult;

    private final Optional<Boolean> differentiallyExpressed;

    private final Optional<Boolean> differentiallySpliced;

    private final Optional<Double> expressionThreshold;

    private final Optional<Double> splicingThreshold;



    /**
     *
     * @param transcripts transcripts encoded by this gene
     * @param transcriptToHitMap Prosite hits for the transcripts
     * @param result result of HBA-DEALS analysis for this gene.
     */
    public AnnotatedGene(List<Transcript> transcripts,
                         Map<AccessionNumber, List<DisplayInterproAnnotation>> transcriptToInterproHitMap,
                         HbaDealsResult result) {
        this.transcripts = transcripts;
        this.transcriptToInterproHitMap = transcriptToInterproHitMap;
        this.hbaDealsResult = result;
        // use HBA Deals results to filter for transcripts that are actually expressed
        Map<AccessionNumber, HbaDealsTranscriptResult> transcriptMap = result.getTranscriptMap();
        expressedTranscripts = transcripts
                    .stream()
                    .filter(t -> transcriptMap.containsKey(t.accessionId()))
                    .collect(Collectors.toList());
        expressedTranscriptMap = new HashMap<>();
        for (Transcript t: transcripts) {
            AccessionNumber accession = t.accessionId();
            if (transcriptMap.containsKey(accession)) {
                double logFC = transcriptMap.get(accession).getLog2FoldChange();
                expressedTranscriptMap.put(t, logFC);
            }
        }
        this.differentiallySpliced = Optional.empty();
        this.differentiallyExpressed = Optional.empty();
        this.expressionThreshold = Optional.empty();
        this.splicingThreshold = Optional.empty();
    }

    public AnnotatedGene(List<Transcript> transcripts,
                         Map<AccessionNumber, List<DisplayInterproAnnotation>> transcriptToInterproHitMap,
                         HbaDealsResult result,
                         double expressionThreshold,
                         double splicingThreshold) {
        this.transcripts = transcripts;
        this.transcriptToInterproHitMap = transcriptToInterproHitMap;
        this.hbaDealsResult = result;
        // use HBA Deals results to filter for transcripts that are actually expressed
        Map<AccessionNumber, HbaDealsTranscriptResult> transcriptMap = result.getTranscriptMap();
        expressedTranscripts = transcripts
                .stream()
                .filter(t -> transcriptMap.containsKey(t.accessionId()))
                .collect(Collectors.toList());
        this.differentiallyExpressed = Optional.of(result.hasDifferentialExpressionResult(expressionThreshold));
        this.differentiallySpliced = Optional.of(result.hasDifferentialSplicingResult(splicingThreshold));
        this.expressionThreshold = Optional.of(expressionThreshold);
        this.splicingThreshold = Optional.of(splicingThreshold);
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

    public int getTranscriptCount() {
        return expressedTranscripts.size();
    }

    public String getSymbol() { return this.hbaDealsResult.getSymbol(); }

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
        return this.differentiallyExpressed.orElse(true);
    }
    /**
     * If a differential expression threshold was provided, return its value. Otherwise we are not thresholding, return true
     * @return true if this gene is differentially spliced
     */
    public boolean passesSplicingThreshold() {
        return this.differentiallySpliced.orElse(true);
    }

    public boolean passesSplicingAndExpressionThreshold() {
        return passesExpressionThreshold() && passesSplicingThreshold();
    }

    public double getSplicingThreshold() {
        return this.splicingThreshold.orElse(1.0);
    }

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


    /**
     * We are sort by whether a gene is differentially spliced and then alphabetically
     */
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
            return this.getHbaDealsResult().getSymbol().compareTo(that.getHbaDealsResult().getSymbol());
        }
    }

    public boolean hasInterproAnnotations() {
        for (AccessionNumber transcriptId : this.transcriptToInterproHitMap.keySet()) {
            if (this.hbaDealsResult.getTranscriptMap().containsKey(transcriptId)) {
                return true;
            }
        }
       return false;
    }


}
