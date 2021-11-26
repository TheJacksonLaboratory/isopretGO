package org.jax.core.visualization;

import org.jax.core.go.GoTermIdPlusLabel;
import org.jax.core.interpro.DisplayInterproAnnotation;

import java.util.List;

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

   String getProteinSvg();

   List<List<String>> getIsoformTableData();


   List<GoTermIdPlusLabel> getGoTerms();

   boolean isDifferentiallyExpressed();

   boolean isDifferentiallySpliced();

   List<DisplayInterproAnnotation>  getInterproForExpressedTranscripts();

   int getI();


}