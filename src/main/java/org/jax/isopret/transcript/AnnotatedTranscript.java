package org.jax.isopret.transcript;

import org.jax.isopret.hbadeals.HbaDealsResult;
import org.jax.isopret.prosite.PrositeHit;

import java.util.List;

public class AnnotatedTranscript {

    private final Transcript transcript;

    private final List<PrositeHit> prositeHits;

    private final HbaDealsResult hbaDealsResult;

    /**
     *
     * @param transcript A transcript object
     * @param hits Protein-level hits with prosite domains
     */
    public AnnotatedTranscript(Transcript transcript,  List<PrositeHit> hits, HbaDealsResult result) {
        this.transcript = transcript;
        this.prositeHits = hits;
        this.hbaDealsResult = result;
    }


    /**
     * Return true if a Prosite motif overlaps an HBADeals specific exon
     * @return
     */
    public boolean hasOverlappingPrositeHit() {
        return false;
    }
}
