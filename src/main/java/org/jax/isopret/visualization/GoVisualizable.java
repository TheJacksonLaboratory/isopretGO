package org.jax.isopret.visualization;

public interface GoVisualizable {

    String termLabel();
    String termId();
    int studyCount();
    int studyTotal();
    int populationCount();
    int populationTotal();
    double pvalue();
    double adjustedPvalue();

}
