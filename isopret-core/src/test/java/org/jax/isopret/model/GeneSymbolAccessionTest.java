package org.jax.isopret.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GeneSymbolAccessionTest {


    @Test
    public void testConstructorAccession() {
        AccessionNumber brca2 = AccessionNumber.ensemblGene("ENSG00000139618.2");
        String geneSymbol = "BRCA2";
        GeneSymbolAccession gsa = new GeneSymbolAccession(geneSymbol, brca2);
        String expectedLabel = "BRCA2 (ENSG00000139618)";
        assertEquals(expectedLabel, gsa.toString());
    }

}
