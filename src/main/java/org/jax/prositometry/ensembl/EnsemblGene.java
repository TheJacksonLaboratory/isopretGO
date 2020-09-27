package org.jax.prositometry.ensembl;

import java.util.HashMap;
import java.util.Map;

public class EnsemblGene {

    private final String geneId;
    private final String geneSymbol;
    private final Map<String, EnsemblTranscript> transcriptMap;

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
}
