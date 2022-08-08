package org.jax.isopret.core;

import org.jax.isopret.core.analysis.InterproOverrepResult;

import java.util.stream.Stream;

public interface InterproAnalysisData {


    Stream<InterproOverrepResult> interproOverrepresentationResults();
}
