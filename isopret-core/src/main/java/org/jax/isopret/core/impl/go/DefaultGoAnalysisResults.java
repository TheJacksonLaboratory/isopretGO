package org.jax.isopret.core.impl.go;

import org.jax.isopret.core.GoAnalysisResults;
import org.jax.isopret.data.GoMethod;
import org.jax.isopret.data.MtcMethod;
import org.monarchinitiative.phenol.analysis.stats.GoTerm2PValAndCounts;

import java.io.File;
import java.util.List;

public record DefaultGoAnalysisResults(File hbaDealsFile,
                                       MtcMethod mtcMethod,
                                       GoMethod goMethod,
                                       List<GoTerm2PValAndCounts> dasGoTerms,
                                       List<GoTerm2PValAndCounts> dgeGoTerms)
        implements GoAnalysisResults {
}
