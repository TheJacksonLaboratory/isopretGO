package org.jax.isopret.interpro;

import org.junit.jupiter.api.Test;

import static org.jax.isopret.interpro.EnsemblStringToInt.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class EnsemblStringToIntTest {


    @Test
    public void testTranscript1() {
        String enstAsString = "ENST00000306609";
        int enstAsInt = 306609;
        int enst = EnsemblStringToInt.transcriptStringToInt("ENST00000306609");
        assertEquals(enstAsInt, enst);
        String result = transcriptIntToString(enst);
        assertEquals(enstAsString, result);
    }


    @Test
    public void testGene1() {
        String ensgAsString = "ENSG00000172288";
        int ensgAsInt = 172288;
        int res = geneStringToInt(ensgAsString);
        assertEquals(ensgAsInt, res);
        String result = geneIntToString(ensgAsInt);
        assertEquals(ensgAsString, result);
    }

}
