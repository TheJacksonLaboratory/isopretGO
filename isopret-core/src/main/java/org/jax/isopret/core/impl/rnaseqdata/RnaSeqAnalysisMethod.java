package org.jax.isopret.core.impl.rnaseqdata;

/**
 * An enumeration to refer to the method used to analyze the RNA-seq dataset we
 * are investigation with this application. Either
 * <p>
 *  Karlebach G, Hansen P, Veiga DF, Steinhaus R, Danis D, Li S, Anczukow O, Robinson PN.
 *  HBA-DEALS: accurate and simultaneous identification of differential expression and splicing using hierarchical Bayesian analysis.
 *  Genome Biol. 2020 Jul 13;21(1):171.  PMID: 32660516.
 * </p>
 * or
 * <p>
 * Robinson MD, McCarthy DJ, Smyth GK.
 * edgeR: a Bioconductor package for differential expression analysis of digital gene expression data.
 * Bioinformatics. 2010 Jan 1;26(1):139-40.  PMID:19910308.
 * </p>
 */
public enum RnaSeqAnalysisMethod {
    HBADEALS, EDGER
}
