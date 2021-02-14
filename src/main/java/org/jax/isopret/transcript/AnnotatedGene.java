package org.jax.isopret.transcript;

import com.fasterxml.jackson.databind.introspect.Annotated;
import org.jax.isopret.hbadeals.HbaDealsResult;
import org.jax.isopret.hbadeals.HbaDealsTranscriptResult;
import org.jax.isopret.prosite.PrositeHit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class AnnotatedGene implements Comparable<AnnotatedGene> {
    /** All annotated transcripts of some gene */
    private final List<Transcript> transcripts;

    /** All annotated transcripts of some gene that were expressed according to HBA deals */
    private final List<Transcript> expressedTranscripts;

    private final Map<String, List<PrositeHit>> transcriptToHitMap;

    private final HbaDealsResult hbaDealsResult;

    private final Optional<Boolean> differentiallyExpressed;

    private final Optional<Boolean> differentiallySpliced;

    /**
     *
     * @param transcripts transcripts encoded by this gene
     * @param transcriptToHitMap Prosite hits for the transcripts
     * @param result result of HBA-DEALS analysis for this gene.
     */
    public AnnotatedGene(List<Transcript> transcripts, Map<String, List<PrositeHit>> transcriptToHitMap, HbaDealsResult result) {
        this.transcripts = transcripts;
        this.transcriptToHitMap = transcriptToHitMap;
        this.hbaDealsResult = result;
        // use HBA Deals results to filter for transcripts that are actually expressed
        Map<String, HbaDealsTranscriptResult> transcriptMap = result.getTranscriptMap();
        expressedTranscripts = transcripts
                    .stream()
                    .filter(t -> transcriptMap.containsKey(t.getAccessionIdNoVersion()))
                    .collect(Collectors.toList());
        this.differentiallySpliced = Optional.empty();
        this.differentiallyExpressed = Optional.empty();
    }

    public AnnotatedGene(List<Transcript> transcripts,
                         Map<String, List<PrositeHit>> transcriptToHitMap,
                         HbaDealsResult result,
                         double expressionThreshold,
                         double splicingThreshold) {
        this.transcripts = transcripts;
        this.transcriptToHitMap = transcriptToHitMap;
        this.hbaDealsResult = result;
        // use HBA Deals results to filter for transcripts that are actually expressed
        Map<String, HbaDealsTranscriptResult> transcriptMap = result.getTranscriptMap();
        expressedTranscripts = transcripts
                .stream()
                .filter(t -> transcriptMap.containsKey(t.getAccessionIdNoVersion()))
                .collect(Collectors.toList());
        this.differentiallyExpressed = Optional.of(result.hasDifferentialExpressionResult(expressionThreshold));
        this.differentiallySpliced = Optional.of(result.hasDifferentialSplicingResult(splicingThreshold));
    }


    public List<Transcript> getExpressedTranscripts() {
        return expressedTranscripts;
    }

    public int getTranscriptCount() {
        return expressedTranscripts.size();
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

    public List<PrositeHit> getPrositeHits(String id) {
        return transcriptToHitMap.getOrDefault(id, new ArrayList<>());
    }

    public Map<String, List<PrositeHit>> getPrositeHitMap() {
        return this.transcriptToHitMap;
    }

    public HbaDealsResult getHbaDealsResult() {
        return hbaDealsResult;
    }

    /**
     * If a differential expression threshold was provided, return its value. Otherwise we are not thresholding, return true
     * @return
     */
    public boolean passesExpressionThreshold() {
        return this.differentiallyExpressed.orElse(true);
    }
    /**
     * If a differential expression threshold was provided, return its value. Otherwise we are not thresholding, return true
     * @return
     */
    public boolean passesSplicingThreshold() {
        return this.differentiallySpliced.orElse(true);
    }


    /**
     * We are sort by whether a gene is differentially spliced and then alphabetically
     * @param that
     * @return
     */
    @Override
    public int compareTo(AnnotatedGene that) {
        if (that==null) return 0;
        if (this.passesSplicingThreshold() && (!that.passesSplicingThreshold())) {
            return 1;
        } else if (that.passesSplicingThreshold() && (!passesSplicingThreshold())) {
            return -1;
        } else {
            return this.getHbaDealsResult().getSymbol().compareTo(that.getHbaDealsResult().getSymbol());
        }
    }
}
