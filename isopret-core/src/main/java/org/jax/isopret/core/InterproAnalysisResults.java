package org.jax.isopret.core;

import org.jax.isopret.core.analysis.InterproOverrepResult;

import java.util.List;

public interface InterproAnalysisResults {

    List<InterproOverrepResult> results();
    int size();
}
