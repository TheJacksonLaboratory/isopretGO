package org.jax.isopret.visualization;

import org.jax.isopret.go.GoTermIdPlusLabel;
import org.jax.isopret.interpro.DisplayInterproAnnotation;

import java.util.List;
import java.util.Map;

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