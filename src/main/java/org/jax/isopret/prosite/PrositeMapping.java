package org.jax.isopret.prosite;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PrositeMapping {

    private final String transcriptID;

    private final String geneID;

    private final List<PrositeHit> hits;

    public PrositeMapping(String transcript, String gene) {
        this.transcriptID = transcript;
        this.geneID = gene;
        hits = new ArrayList<>();
    }

    public void addPrositeHit(String ac, int begin, int end) {
        hits.add(new PrositeHit(ac, begin, end));
    }

    public String getTranscriptID() {
        return transcriptID;
    }

    public String getGeneID() {
        return geneID;
    }

    public List<PrositeHit> getHits() {
        Collections.sort(hits);
        return hits;
    }
}
