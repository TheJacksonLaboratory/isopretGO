package org.jax.isopret.core;

import org.jax.isopret.core.impl.go.DefaultIsopretGoAnalysisRunner;
import org.jax.isopret.core.impl.rnaseqdata.RnaSeqAnalysisMethod;
import org.jax.isopret.model.GoMethod;
import org.jax.isopret.model.MtcMethod;

import java.io.File;

public interface IsopretGoAnalysisRunner {

    GoAnalysisResults run();

    static IsopretGoAnalysisRunner hbadeals(IsopretProvider provider,
                                            File hbaDealsFile,
                                            MtcMethod mtcMethod,
                                            GoMethod goMethod) {
        return new DefaultIsopretGoAnalysisRunner(provider, hbaDealsFile, mtcMethod, goMethod, RnaSeqAnalysisMethod.HBADEALS);
    }

    static IsopretGoAnalysisRunner edgeR(IsopretProvider provider,
                                         File hbaDealsFile,
                                         MtcMethod mtcMethod,
                                         GoMethod goMethod) {
        return new DefaultIsopretGoAnalysisRunner(provider, hbaDealsFile, mtcMethod, goMethod, RnaSeqAnalysisMethod.EDGER);
    }

    void exportAll();
}
