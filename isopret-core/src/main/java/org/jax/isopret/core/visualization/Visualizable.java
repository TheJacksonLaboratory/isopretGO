package org.jax.isopret.core.visualization;

import org.jax.isopret.core.interpro.DisplayInterproAnnotation;
import org.monarchinitiative.phenol.ontology.data.TermId;

import java.util.List;
import java.util.Set;

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

    String getGeneEnsemblUrl();

    String getChromosome();

    int getDifferentialTranscriptCount();

    int getExpressedTranscriptCount();

    int getTotalTranscriptCount();

    int getCodingTranscriptCount();

    double getExpressionPep();

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

    boolean isDifferentiallyExpressed();

    boolean isDifferentiallySpliced();

    String getNofMsplicing();

    double getBestSplicingPval();

    List<DisplayInterproAnnotation> getInterproForExpressedTranscripts();

    List<InterproVisualizable> getInterproVisualizable();

    GoAnnotationMatrix getGoAnnotationMatrix();

    String getGoHtml();

    Set<TermId> getAnnotationGoIds();

    DoublePepValue getExpressionPepValue();
    DoublePepValue getSplicingPepValue();

}