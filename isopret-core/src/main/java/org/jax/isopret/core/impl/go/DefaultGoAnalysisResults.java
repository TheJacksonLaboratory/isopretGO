package org.jax.isopret.core.impl.go;

import org.jax.isopret.core.GoAnalysisResults;
import org.jax.isopret.model.GoMethod;
import org.jax.isopret.model.MtcMethod;
import org.monarchinitiative.phenol.analysis.stats.GoTerm2PValAndCounts;

import java.util.List;

public class DefaultGoAnalysisResults implements GoAnalysisResults  {

    private final String hbaDealsFile;
    private final MtcMethod mtcMethod;
    private final GoMethod goMethod;
    private final List<GoTerm2PValAndCounts> dasGoTerms;
    private final List<GoTerm2PValAndCounts> dgeGoTerms;

    public DefaultGoAnalysisResults(String hbaDealsFile, MtcMethod mtcMethod, GoMethod goMethod, List<GoTerm2PValAndCounts> dasGoTerms, List<GoTerm2PValAndCounts> dgeGoTerms) {
        this.hbaDealsFile = hbaDealsFile;
        this.mtcMethod = mtcMethod;
        this.goMethod = goMethod;
        this.dasGoTerms = dasGoTerms;
        this.dgeGoTerms = dgeGoTerms;
    }

    @Override
    public String hbaDealsFile() {
        return null;
    }

    @Override
    public MtcMethod mtcMethod() {
        return null;
    }

    @Override
    public GoMethod goMethod() {
        return null;
    }

    @Override
    public List<GoTerm2PValAndCounts> dasGoTerms() {
        return null;
    }

    @Override
    public List<GoTerm2PValAndCounts> dgeGoTerms() {
        return null;
    }
}
