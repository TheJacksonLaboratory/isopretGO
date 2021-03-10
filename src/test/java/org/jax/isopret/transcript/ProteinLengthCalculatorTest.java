package org.jax.isopret.transcript;

import org.jax.isopret.TestBase;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ProteinLengthCalculatorTest extends TestBase {
    /** This map has keys -- Ensembl accession ids for ADAR; values -- corresponding Transcript objects. */
    private final static Map<AccessionNumber, Transcript> map = getAdarAnnotatedTranscript().getTranscripts().stream()
            .collect(Collectors.toMap(Transcript::accessionId, transcript -> transcript));



    @Test
    public void test1() {
        for (AccessionNumber s : map.keySet()) {
            System.out.println(s);
        }

    }


    @Test
    public void testENST00000463920() {
        // this is a noncoding isoform
        var ENST00000463920 = AccessionNumber.ensemblTranscript("ENST00000463920");
        Transcript ENST00000463920result = map.get(ENST00000463920);
        assertEquals(0, ENST00000463920result.getProteinLength());
    }

    @Test
    public void testENST00000534279() {
        // this is a noncoding isoform
        var ENST00000534279 = AccessionNumber.ensemblTranscript("ENST00000534279");
        Transcript ENST00000534279result = map.get(ENST00000534279);
        assertEquals(0, ENST00000534279result.getProteinLength());
    }

    @Test
    public void testENST00000368474() {
        // len 1226 aa
        var ENST00000368474 = AccessionNumber.ensemblTranscript("ENST00000368474");
        Transcript ENST00000368474result = map.get(ENST00000368474);
        int len = ENST00000368474result.getProteinLength();
        assertEquals(1226, len);
    }

    @Test
    public void testENST00000368471() {
        // len 931 aa
        Transcript ENST00000368471 = map.get(AccessionNumber.ensemblTranscript("ENST00000368471"));
        assertEquals(931, ENST00000368471.getProteinLength());
    }

    // TODO  -- there is a discrepancy to Ensembl, unsure if this is correct or not
    @Test
    public void testENST00000529168() {
        // len 1200 aa
        Transcript ENST00000529168 = map.get(AccessionNumber.ensemblTranscript("ENST00000529168"));
      //  assertEquals(1200, ENST00000529168.getProteinLength());
    }

    @Test
    public void testENST00000494866() {
        // this is a noncoding isoform
        Transcript ENST00000494866 = map.get(AccessionNumber.ensemblTranscript("ENST00000494866"));
        assertEquals(0, ENST00000494866.getProteinLength());
    }

    @Test
    public void testENST00000530954() {
        // this is a noncoding isoform
        Transcript ENST00000530954 = map.get(AccessionNumber.ensemblTranscript("ENST00000530954"));
        assertEquals(0, ENST00000530954.getProteinLength());
    }

    @Test
    public void testENST00000526905() {
        // this is a noncoding isoform
        Transcript ENST00000526905 = map.get(AccessionNumber.ensemblTranscript("ENST00000526905"));
        assertEquals(0, ENST00000526905.getProteinLength());
    }

    @Test
    public void testENST00000492630() {
        // this is a noncoding isoform
        Transcript ENST00000492630 = map.get(AccessionNumber.ensemblTranscript("ENST00000492630"));
        assertEquals(0, ENST00000492630.getProteinLength());
    }

    @Test
    public void testENST00000471068() {
        // this is a noncoding isoform
        Transcript ENST00000471068 = map.get(AccessionNumber.ensemblTranscript("ENST00000471068"));
        assertEquals(0, ENST00000471068.getProteinLength());
    }


}
