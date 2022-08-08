package org.jax.isopret.core;

import org.jax.isopret.core.impl.go.DefaultGoAnalysisResults;
import org.jax.isopret.core.impl.go.DefaultIsopretGoAnalysisRunner;
import org.jax.isopret.model.GoMethod;
import org.jax.isopret.model.MtcMethod;

import java.util.concurrent.RecursiveTask;

public interface IsopretGoAnalysisRunner {

    GoAnalysisResults run();

    static IsopretGoAnalysisRunner hbadeals(IsopretProvider provider , String hbaDealsFile, MtcMethod mtcMethod, GoMethod goMethod) {
        return new DefaultIsopretGoAnalysisRunner(provider, hbaDealsFile, mtcMethod, goMethod);
    }
}
