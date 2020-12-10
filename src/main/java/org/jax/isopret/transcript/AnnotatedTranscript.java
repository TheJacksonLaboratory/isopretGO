package org.jax.isopret.transcript;

import org.jax.isopret.hbadeals.HbaDealsResult;
import org.jax.isopret.prosite.PrositeHit;

import java.util.List;

public class AnnotatedTranscript {
    /** All annotated transcripts of some gene */
    private final List<Transcript> transcripts;

    /** All annotated transcripts of some gene */
    private final List<Transcript> expressedTranscripts;

    private final List<PrositeHit> prositeHits;

    private final HbaDealsResult hbaDealsResult;

    /**
     *
     * @param transcripts
     * @param hits Protein-level hits with prosite domains
     */
    public AnnotatedTranscript(List<Transcript> transcripts,  List<PrositeHit> hits, HbaDealsResult result) {
        this.transcripts = transcripts;
        this.prositeHits = hits;
        this.hbaDealsResult = result;
        // use HBA Deals results to filter for transcripts that are actually expressed
        expressedTranscripts = List.of(); // todo
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

    public List<PrositeHit> getPrositeHits() {
        return prositeHits;
    }

    public HbaDealsResult getHbaDealsResult() {
        return hbaDealsResult;
    }
}
