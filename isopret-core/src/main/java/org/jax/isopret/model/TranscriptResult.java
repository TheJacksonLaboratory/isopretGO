package org.jax.isopret.model;

import org.jax.isopret.data.AccessionNumber;

public interface TranscriptResult {
    String getTranscript();

    AccessionNumber getTranscriptId();

    double getFoldChange();

    double getLog2FoldChange() ;

    double getPvalue();

    boolean isSignificant(double threshold);
}
