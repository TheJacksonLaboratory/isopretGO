package org.jax.isopret.visualization;

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


}