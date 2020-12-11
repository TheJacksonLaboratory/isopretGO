package org.jax.isopret.visualization;

import org.jax.isopret.hbadeals.HbaDealsResult;
import org.jax.isopret.prosite.PrositeHit;
import org.jax.isopret.transcript.AnnotatedGene;
import org.jax.isopret.transcript.Transcript;

import java.util.List;
import java.util.Map;

public class HrmlVisualizable implements Visualizable {


     /** Number of all annotated transcripts of some gene */
    private final int totalTranscriptCount;

    /** All annotated transcripts of some gene that were expressed according to HBA deals */
    private final List<Transcript> expressedTranscripts;

    private final Map<String, List<PrositeHit>> transcriptToHitMap;

    private final HbaDealsResult hbaDealsResult;


    public HrmlVisualizable(AnnotatedGene agene) {
        this.totalTranscriptCount = agene.getTranscripts().size();
        this.expressedTranscripts = agene.getExpressedTranscripts();
        this.transcriptToHitMap = agene.getPrositeHitMap();
        this.hbaDealsResult = agene.getHbaDealsResult();
    }

    @Override
    public String getGeneSymbol() {
        return hbaDealsResult.getSymbol();
    }

    @Override
    public double getExpressionPval() {
        return this.hbaDealsResult.getExpressionP();
    }

    @Override
    public double getMostSignificantSplicingPval() {
        return this.hbaDealsResult.getMostSignificantSplicingPval();
    }
}
