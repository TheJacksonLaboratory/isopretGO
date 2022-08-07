package org.jax.isopret.core.impl.interpro;

import org.jax.isopret.core.InterproAnalysisResults;
import org.jax.isopret.core.analysis.InterproOverrepResult;

import java.util.List;
import java.util.Objects;

public class InterproResultsDefault implements InterproAnalysisResults {
    private static final InterproResultsDefault EMPTY = new InterproResultsDefault(List.of());

    static InterproResultsDefault empty() {
        return EMPTY;
    }

    private final List<InterproOverrepResult> results;

    InterproResultsDefault(List<InterproOverrepResult> results) {
        this.results = Objects.requireNonNull(results);
    }

    @Override
    public List<InterproOverrepResult> results() {
        return results;
    }

    @Override
    public int size() {
        return results.size();
    }
}
