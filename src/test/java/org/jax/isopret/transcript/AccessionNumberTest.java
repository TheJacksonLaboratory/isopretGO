package org.jax.isopret.transcript;

import org.jax.isopret.except.IsopretRuntimeException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AccessionNumberTest {

    @Test
    public void testWithoutAccession() {
        String id = "ENSG00000139618";
        int expected = 139618;
        assertEquals(expected, AccessionNumber.ensemblGene(id).getAccessionNumber());
    }

    @Test
    public void testWithAccession() {
        String id = "ENSG00000139618.2";
        int expected = 139618;
        assertEquals(expected, AccessionNumber.ensgAccessionToInt(id));
    }

    @Test
    public void testBacktoString() {
        String expected = "ENSG00000139618";
        int id = 139618;
        assertEquals(expected, AccessionNumber.ensgAccessionToString(id));
    }

    @Test
    public void testMalformedGeneId() {
        String transcriptId = "ENST00000139618"; // transcript, should throw error
        Assertions.assertThrows(IsopretRuntimeException.class,() ->AccessionNumber.ensgAccessionToInt(transcriptId));
    }


    @Test
    public void testTranscriptWithoutAccession() {
        String id = "ENST00000560355";
        int expected = 560355;
        assertEquals(expected, AccessionNumber.enstAccessionToInt(id));
    }

    @Test
    public void testTranscriptWithAccession() {
        String id = "ENST00000560355.1";
        int expected = 560355;
        assertEquals(expected, AccessionNumber.enstAccessionToInt(id));
    }

    @Test
    public void testTranscriptBacktoString() {
        String expected = "ENST00000560355";
        int id = 560355;
        assertEquals(expected, AccessionNumber.enstAccessionToString(id));
    }

    @Test
    public void testMalformedTranscriptId() {
        String geneId = "ENSG00000139618"; // gene, should throw error
        Assertions.assertThrows(IsopretRuntimeException.class,() ->AccessionNumber.enstAccessionToInt(geneId));
    }
}
