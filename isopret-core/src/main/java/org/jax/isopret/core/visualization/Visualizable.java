package org.jax.isopret.core.visualization;

import org.jax.isopret.core.interpro.DisplayInterproAnnotation;

import java.util.List;

/**
 * Interface for classes that help to display the results
 * for an indivudal gene. See {@link EnsemblVisualizable}
 * for an implementation.
 *
 * @author Peter Robinson
 */
public interface Visualizable {

    String getGeneSymbol();

    String getGeneAccession();

    String getGeneUrl();

    String getChromosome();

    int getExpressedTranscriptCount();

    int getTotalTranscriptCount();

    double getExpressionPval();

    double getExpressionFoldChange();

    double getExpressionLogFoldChange();

    double getMostSignificantSplicingPval();

    String getIsoformSvg();

    int getIsoformSvgHeight();

    String getIsoformHtml();

    String getProteinSvg();

    String getProteinHtml();

    int getProteinSvgHeight();

    List<IsoformVisualizable> getIsoformVisualizable();


    List<OntologyTermVisualizable> getGoTerms();

    boolean isDifferentiallyExpressed();

    boolean isDifferentiallySpliced();

    String getNofMsplicing();

    double getBestSplicingPval();

    List<DisplayInterproAnnotation> getInterproForExpressedTranscripts();

    List<InterproVisualizable> getInterproVisualizable();


    int getI();


}