package org.jax.isopret.transcript;

import org.jax.isopret.prosite.PrositeHit;

import java.util.List;

public class AnnotatedTranscript {

    private final Transcript transcript;

    private final List<PrositeHit> prositeHits;

    /**
     *
     * @param transcript A transcript object
     * @param hits Protein-level hits with prosite domains
     */
    public AnnotatedTranscript(Transcript transcript,  List<PrositeHit> hits) {
        this.transcript = transcript;
        this.prositeHits = hits;
    }
}
