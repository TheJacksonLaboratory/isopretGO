package org.jax.isopret.prosite;

import java.util.*;

public class PrositeMapping {

    private final String transcriptID;

    private final String geneID;

    private final Map<String, List<PrositeHit>> transcriptToPrositeListMap;


    public PrositeMapping(String transcript, String gene) {
        this.transcriptID = transcript;
        this.geneID = gene;
        transcriptToPrositeListMap = new HashMap<>();
    }

    public void addPrositeHit(String transcriptID, String ac, int begin, int end) {
        transcriptToPrositeListMap.putIfAbsent(transcriptID, new ArrayList<>());
        transcriptToPrositeListMap.get(transcriptID).add(new PrositeHit(ac, begin, end));
    }

    public String getTranscriptID() {
        return transcriptID;
    }

    public String getGeneID() {
        return geneID;
    }

    public List<PrositeHit> getHits(String transcriptID) {
        if (transcriptToPrositeListMap.containsKey(transcriptID)) {
            List<PrositeHit> hits = transcriptToPrositeListMap.get(transcriptID);
            Collections.sort(hits);
            return hits;
        } else {
            return List.of();
        }
    }

    public Map<String, List<PrositeHit>> getTranscriptToPrositeListMap() {
        return transcriptToPrositeListMap;
    }
}
