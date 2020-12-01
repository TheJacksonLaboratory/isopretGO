package org.jax.isopret.ensembl;

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;

import java.util.*;

public class EnsemblGene {

    private final String geneId;
    private final String geneSymbol;
    private final Map<String, EnsemblTranscript> transcriptMap;
    private boolean mapsDifferent = false;

    public EnsemblGene(EnsemblTranscript transcript) {
        this.geneId = transcript.getGeneId();
        this.geneSymbol = transcript.getGeneSymbol();
        this.transcriptMap = new HashMap<>();
    }

    public void addTranscript(EnsemblTranscript tr) {
        this.transcriptMap.put(tr.getTranscriptId(), tr);
    }

    public String getGeneId() {
        return geneId;
    }

    public String getGeneSymbol() {
        return geneSymbol;
    }

    public Map<String, EnsemblTranscript> getTranscriptMap() {
        return transcriptMap;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(geneSymbol).append(" - ").append(geneId);
        for (EnsemblTranscript et : transcriptMap.values()) {
            sb.append("\n").append(et.toString());
        }
        return sb.toString();
    }

    public void setMapDifference(boolean different) {
        mapsDifferent = different;
    }

    public boolean mapsAreDifferent() {
        return mapsDifferent;
    }

    /**
     * Return a set of motifs that are different between the ones corresponding to transcriptID
     * and those annotating one or more other transcripts.
     * @param transcriptID transcript of current reference
     * @return Set of Motif names that differ
     */
    public Set<String> getDifference(String transcriptID) {
        if (! transcriptMap.containsKey(transcriptID)) {
            System.err.println("[ERROR] Could not find transcript ID: " + transcriptID);
            return Set.of();
        }
        EnsemblTranscript key = transcriptMap.get(transcriptID);
        Set<String> differingMotifs = new HashSet<>();
        for (EnsemblTranscript et : transcriptMap.values()) {
            if (transcriptID.equalsIgnoreCase(et.getTranscriptId())) {
                continue; // skip, this element is the same as transcriptID
            }
            MapDifference<String, List<Integer>> assignDiff = Maps.difference(key.getMotifMap(),et.getMotifMap());
            differingMotifs.addAll(assignDiff.entriesDiffering().keySet());
        }
        return differingMotifs;
    }
}
