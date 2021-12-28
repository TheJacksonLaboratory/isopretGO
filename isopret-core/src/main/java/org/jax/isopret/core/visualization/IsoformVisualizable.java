package org.jax.isopret.core.visualization;

public interface IsoformVisualizable {
    String transcriptAccession();
    String isoformUrlAnchor();
    String isoformP();
    String log2Foldchange();
    boolean isSignificant();
}
