package org.jax.isopret.model;

import org.jax.isopret.data.AccessionNumber;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AccessionNumberTest {


    @Test
    public void testGeneAccession() {
        AccessionNumber gene = AccessionNumber.ensemblGene("ENSG00000139618.2");
        // We discard the version number
        String expectedAccession = "ENSG00000139618";
        assertEquals(expectedAccession, gene.getAccessionString());
        // We store the accession as an int in some places
        int expectedAccessionInt = 139618;
        assertEquals(expectedAccessionInt, gene.getAccessionNumber());
        assertTrue(gene.isGene());
        assertFalse(gene.isTranscript());
    }


    @Test
    public void testTranscriptAccession() {
        AccessionNumber transcript = AccessionNumber.ensemblTranscript("ENST00000369985.3");
        // We discard the version number
        String expectedAccession = "ENST00000369985";
        assertEquals(expectedAccession, transcript.getAccessionString());
        // We store the accession as an int in some places
        int expectedAccessionInt = 369985;
        assertEquals(expectedAccessionInt, transcript.getAccessionNumber());
        assertTrue(transcript.isTranscript());
        assertFalse(transcript.isGene());
    }
}
