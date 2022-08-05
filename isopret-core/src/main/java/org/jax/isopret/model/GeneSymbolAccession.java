package org.jax.isopret.model;
/**
 * A convenience class to record both the symbol and the accession number of a gene.
 */
public record GeneSymbolAccession(String symbol, AccessionNumber accession) {


    @Override
    public String toString() {
        return symbol + " (" + accession.getAccessionString() + ")";
    }

}
