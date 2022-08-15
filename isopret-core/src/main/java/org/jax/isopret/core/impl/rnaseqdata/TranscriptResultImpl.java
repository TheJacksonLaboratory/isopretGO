package org.jax.isopret.core.impl.rnaseqdata;

import org.jax.isopret.model.TranscriptResult;
import org.jax.isopret.model.AccessionNumber;


/**
 * This class stores the results of analysis of differential splicing of
 * a specific transcript (isoform) of a gene. It is expected that instances of
 * this class will be contained in a member list of the {@link GeneResultImpl} object
 * that represents the gene to which this isoform belongs.
 * @author Peter N Robinson
 */
public class TranscriptResultImpl implements TranscriptResult {
    private final AccessionNumber transcript;
    private final double foldChange;
    private final double P;


    public TranscriptResultImpl(AccessionNumber transcript, double fc, double p) {
        this.transcript = transcript;
        this.foldChange = fc;
        this.P = p;
    }

    @Override
    public String getTranscript() {
        return transcript.getAccessionString();
    }
    @Override
    public AccessionNumber getTranscriptId() {
        return transcript;
    }
    @Override
    public double getFoldChange() {
        return foldChange;
    }
    @Override
    public double getLog2FoldChange() {
        if (foldChange == 0.0) return foldChange;
        return Math.log(foldChange)/Math.log(2.0);
    }
    @Override
    public double getPvalue() {
        return P;
    }
    @Override
    public boolean isSignificant(double threshold) {
        return this.P < threshold;
    }
}
