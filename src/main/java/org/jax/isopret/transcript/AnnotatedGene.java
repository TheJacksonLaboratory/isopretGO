package org.jax.isopret.transcript;

import org.jax.isopret.hbadeals.HbaDealsResult;
import org.jax.isopret.hbadeals.HbaDealsTranscriptResult;
import org.jax.isopret.prosite.PrositeHit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AnnotatedGene {
    /** All annotated transcripts of some gene */
    private final List<Transcript> transcripts;

    /** All annotated transcripts of some gene that were expressed according to HBA deals */
    private final List<Transcript> expressedTranscripts;

    Map<String, List<PrositeHit>> transcriptToHitMap;

    private final HbaDealsResult hbaDealsResult;

    /**
     *
     * @param transcripts

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
    }


    /**
     * Return true if a Prosite motif overlaps an HBADeals specific exon
     * and we have at least one significant differential splicing event
     * @return
     */
    public boolean hasOverlappingPrositeHit() {
        // among expressed transcripts, are the motifs different for differential vs other
        return false;
    }


    public List<Transcript> getExpressedTranscripts() {
        return expressedTranscripts;
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
}
