package org.jax.isopret.visualization;

import org.jax.isopret.go.GoTermIdPlusLabel;
import org.jax.isopret.interpro.DisplayInterproAnnotation;
import org.jax.isopret.interpro.InterproEntry;

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

   String getProteinSvg(Map<String, String> prositeIdToName);

   List<List<String>> getIsoformTableData();

   List<List<String>> getPrositeModuleLinks(Map<String, String> prositeIdToName);

   List<GoTermIdPlusLabel> getGoTerms();

   boolean isDifferentiallyExpressed();

   boolean isDifferentiallySpliced();

   List<DisplayInterproAnnotation>  getInterproForExpressedTranscripts();

   int getI();


}