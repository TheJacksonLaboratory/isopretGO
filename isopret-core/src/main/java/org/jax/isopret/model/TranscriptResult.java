package org.jax.isopret.model;

public interface TranscriptResult {
    String getTranscript();

    AccessionNumber getTranscriptId();

    double getFoldChange();

    double getLog2FoldChange() ;

    double getPvalue();

    boolean isSignificant(double threshold);
}
