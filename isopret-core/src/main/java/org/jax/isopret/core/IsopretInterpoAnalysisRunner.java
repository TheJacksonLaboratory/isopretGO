package org.jax.isopret.core;

import org.jax.isopret.core.impl.interpro.IsopretInterpoAnalysisRunnerImpl;
import org.jax.isopret.model.AnnotatedGene;

import java.util.List;

public interface IsopretInterpoAnalysisRunner {

    InterproAnalysisResults run();

    static IsopretInterpoAnalysisRunner hbadeals(String hbadealsFile, IsopretProvider provider, double splicingPep) {
        return IsopretInterpoAnalysisRunnerImpl.of(hbadealsFile, provider);
    }

    /**
     * This will run the analysis starting from a list of {@link  AnnotatedGene} objects, which is the case from
     * the GUI
     * @param annotatedGeneList
     * @param splicingPep Splicing PEP threshold
     * @return
     */
    static IsopretInterpoAnalysisRunner hbadeals(List<AnnotatedGene> annotatedGeneList, double splicingPep) {
        return IsopretInterpoAnalysisRunnerImpl.of(annotatedGeneList, splicingPep);
    }

}

