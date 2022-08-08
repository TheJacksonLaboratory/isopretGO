package org.jax.isopret.core.impl.go;

import org.jax.isopret.core.GoAnalysisResults;
import org.jax.isopret.model.GoMethod;
import org.jax.isopret.model.MtcMethod;
import org.monarchinitiative.phenol.analysis.stats.GoTerm2PValAndCounts;

import java.util.List;

public record DefaultGoAnalysisResults(String hbaDealsFile,
                                       MtcMethod mtcMethod,
                                       GoMethod goMethod,
                                       List<GoTerm2PValAndCounts> dasGoTerms,
                                       List<GoTerm2PValAndCounts> dgeGoTerms)
        implements GoAnalysisResults {
}
