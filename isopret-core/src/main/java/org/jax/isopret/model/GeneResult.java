package org.jax.isopret.model;

import org.jax.isopret.data.AccessionNumber;

import java.util.*;

public interface GeneResult {


    AccessionNumber getGeneAccession();
    /**
     * @return an int representing Accession number of the gene, e.g., 1167 for ENSG00000001167. */
    int getEnsgId();

    GeneModel getGeneModel();

    GeneSymbolAccession getGeneSymbolAccession() ;

    double getExpressionFoldChange();

    double getExpressionLog2fc();

    double getExpressionP();

    List<Double> getSplicingPlist() ;

    void addExpressionResult(double fc, double p);
    void addTranscriptResult(AccessionNumber isoform, double expFC, double P);


    Map<AccessionNumber, TranscriptResult> getTranscriptMap();

    /**
     * Only expressed transcripts are added to the HBA-DEALS results file.
     * @return Number of expressed transcripts observed for this gene.
     */
    int getExpressedTranscriptCount();

    int getSignificantTranscriptCount(double pepThreshold) ;

    boolean hasDifferentialExpressionResult(double threshold) ;


    boolean hasDifferentialSplicingResult(double threshold) ;

    /**
     *
     * @param splicing adjusted probability threshold
     * @param expression adjusted probability threshold
     * @return true if this gene has a differential expression OR splicing result
     */
    boolean hasDifferentialSplicingOrExpressionResult(double splicing, double expression) ;

    boolean transcriptExpressed(AccessionNumber acc);


    double getSmallestSplicingP();


    Set<TranscriptResult> getTranscriptResults();


}
