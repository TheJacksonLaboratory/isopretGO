package org.jax.isopret.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class InterproAnnotationTest {
    private static final AccessionNumber transcriptAccession = AccessionNumber.ensemblTranscript("ENST00000635775");
    private static final AccessionNumber geneAccession = AccessionNumber.ensemblGene("ENSG00000139618.2");

    private static final int interproAccessionNumber = 424242;
    private static final InterproAnnotation interproAnnotation = new InterproAnnotation(transcriptAccession, geneAccession, interproAccessionNumber, 7, 31);

    @Test
    public void testCtor() {
        assertEquals(transcriptAccession, interproAnnotation.getEnst());
        assertEquals(geneAccession, interproAnnotation.getEnsg());
        assertEquals(interproAccessionNumber, interproAnnotation.getInterpro());
        assertEquals(7, interproAnnotation.getStart());
        assertEquals(31, interproAnnotation.getEnd());
        int length = 31 - 7 + 1;
        assertEquals(length, interproAnnotation.getLength());
    }


}
