package org.jax.isopret.visualization;

/**
 * Interface for classes that help display results for isoforms.
 * See {@link EnsemblGeneIsoformVisualizable} for the implementation
 * we are currently using.
 * @author Peter Robinson
 */
public interface IsoformVisualizable {
    String transcriptAccession();
    String isoformUrlAnchor();
    String isoformP();
    String log2Foldchange();
    boolean isSignificant();
}
