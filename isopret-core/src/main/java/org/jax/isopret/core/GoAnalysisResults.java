package org.jax.isopret.core;

import org.jax.isopret.model.GoMethod;
import org.jax.isopret.model.MtcMethod;
import org.monarchinitiative.phenol.analysis.stats.GoTerm2PValAndCounts;

import java.io.File;
import java.util.List;

public interface GoAnalysisResults {

    File hbaDealsFile();

    MtcMethod mtcMethod();

    GoMethod goMethod();

    List<GoTerm2PValAndCounts> dasGoTerms();

    List<GoTerm2PValAndCounts> dgeGoTerms();
}