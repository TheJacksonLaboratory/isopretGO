package test.java.org.jax.isopret.prosite;

import org.jax.core.prosite.PrositePattern;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PrositePatternTest {




    private PrositePattern createPattern(String pattern) {
        String ID = "FAKE"; // not needed for test
        String AC = "P-FAKE"; // not needed for test
        String DE = "FAKE"; // not needed for test
        return new PrositePattern(ID, AC, DE, pattern);
    }

    /**
     * ID: RTC; AC: PS01287; PA: [RH]-G-x(2)-P-x-G(3)-x-[LIV]
     * Here are some examples  RGmpPgGGGeV, RGapPnGGGsV, HGfyPaGGGvV, RGyyPkGGGeV
     */
    @Test
    public void testRTCMatch() {
        String pattern = "[RH]-G-x(2)-P-x-G(3)-x-[LIV]";
        String example1 = "RGmpPgGGGeV".toUpperCase();
        String example2 = "RGapPnGGGsV".toUpperCase();
        String example3 = "HGfyPaGGGvV".toUpperCase();
        String example4 = "RGyyPkGGGeV".toUpperCase();
        PrositePattern propat = createPattern(pattern);
        assertTrue(propat.matchesSequence(example1));
        assertTrue(propat.matchesSequence(example2));
        assertTrue(propat.matchesSequence(example3));
        assertTrue(propat.matchesSequence(example4));
        String badExample1 = "RGmpPgGAGeV".toUpperCase();
        String badExample2 = "MGmpPgGAGeV".toUpperCase();
        String badExample3 = "HGfyPaGG".toUpperCase();
        String badExample4 = "ZGyyPkGGGeV".toUpperCase();
        assertFalse(propat.matchesSequence(badExample1));
        assertFalse(propat.matchesSequence(badExample2));
        assertFalse(propat.matchesSequence(badExample3));
        assertFalse(propat.matchesSequence(badExample4));
    }

    /**
     * ID: RTC; AC: PS01287; PA: [RH]-G-x(2)-P-x-G(3)-x-[LIV]
     * Same as above, but find the correct list of locations
     */
    @Test
    public void testRTCLocation() {
        String pattern = "[RH]-G-x(2)-P-x-G(3)-x-[LIV]";
        PrositePattern propat = createPattern(pattern);
        // here, the pattern begins at the fifth letter (index 4)
        // there is only one hit
        String sequence1 = "CGHWRGmpPgGGGeVDDW";
        List<Integer> positions = propat.getPositions(sequence1);
        assertEquals(1, positions.size());
        int pos = positions.get(0);
        assertEquals(4, pos);
        // here, the pattern begins at the index 4 and 22
        // there are two hits
        String sequence2 = "CGHWRGmpPgGGGeVDDWCGHWRGmpPgGGGeVDDW";
        positions = propat.getPositions(sequence2);
        assertEquals(2, positions.size());
        assertEquals(4, positions.get(0));
        assertEquals(22, positions.get(1));
    }

    /**
     *  VEGFR2 contains 21 N-X-S/T sequons, where three N at positions 143, 245, and 318 are experimentally validated.
     *  nknktvvipc  NKTV
     *  klvlnctart NCTA
     *  sglmtkknst f  NSTF
     *
     */
    @Test
    public void testASN_GLYCOSYLATION() {
        String pattern = "N-{P}-[ST]-{P}";
        PrositePattern propat = createPattern(pattern);
        String example1 = "NKTV";
        String example2 = "NCTA";
        String example3 = "NSTF";
        assertTrue(propat.matchesSequence(example1));
        assertTrue(propat.matchesSequence(example2));
        assertTrue(propat.matchesSequence(example3));
        String badExample1 = "NKT";
        String badExample2 = "NPTA";
        String badExample3 = "NSTP";
        assertFalse(propat.matchesSequence(badExample1));
        assertFalse(propat.matchesSequence(badExample2));
        assertFalse(propat.matchesSequence(badExample3));
    }

    @Test
    public void testASN_GLYCOSYLATIONLocation() {
        String pattern = "N-{P}-[ST]-{P}";
        PrositePattern propat = createPattern(pattern);
        String sequence1 = "nknktvvipc".toUpperCase(); // index 2
        List<Integer> positions = propat.getPositions(sequence1);
        assertEquals(1, positions.size());
        int pos = positions.get(0);
        assertEquals(2, pos);
    }

    /**
     * CAMP_PHOSPHO_SITE; PS00004;; [RK](2)-x-[ST].
     * //  Val-Leu-Gln-Arg-Arg-Arg-Gly-Ser-Ser-Ile-Pro-Gln is mention in PMID: 3005275
     * RRGS
     * VLQRRRGSSIPQ
     */
    @Test
    public void testCampPhosphoSite() {
        String pattern = "[RK](2)-x-[ST]";
        PrositePattern propat = createPattern(pattern);
        String sequence1 = "RRGS"; // index 2
        assertTrue(propat.matchesSequence(sequence1));
        String sequence2 = "VLQRRRGSSIPQ"; // starts at i=4
        List<Integer> positions = propat.getPositions(sequence2);
        assertEquals(1, positions.size());
        int pos = positions.get(0);
        assertEquals(4, pos);
        String sequence3 = "VLQRRRGSSIPQRRSTAA"; // starts at i=4 and i=12
        positions = propat.getPositions(sequence3);
        assertEquals(2, positions.size());
        assertEquals(4, positions.get(0));
        assertEquals(12, positions.get(1));
    }

    /**
     * GLA_1; PS00011;; E-x(2)-[ERK]-E-x-C-x(6)-[EDR]-x(10,11)-[FYA]-[YW].
     * Example EciEErCskeearEafeddektetFW
     */
    @Test
    public void testGLA_2() {
        String pattern = "E-x(2)-[ERK]-E-x-C-x(6)-[EDR]-x(10,11)-[FYA]-[YW]";
        PrositePattern propat = createPattern(pattern);
        String sequence1 = "EciEErCskeearEafeddektetFW".toUpperCase();
        assertTrue(propat.matchesSequence(sequence1));
        String badSequence1 = "EciWErCskeearEafeddektetFW".toUpperCase();
        assertFalse(propat.matchesSequence(badSequence1));
    }

    @Test
    public void testER_TARGET() {
        String pattern = "[KRHQSA]-[DENQ]-E-L>";
        PrositePattern propat = createPattern(pattern);
        String sequence1 = "RDEL";
        assertTrue(propat.matchesSequence(sequence1));
        String badSeqeunce = "RDELA"; // not a match, needs to be at the end
        assertFalse(propat.matchesSequence(badSeqeunce));
    }

    @Test
    public void testTUBULIN_B_AUTOREG() {
        String pattern = "<M-R-[DE]-[IL]";
        PrositePattern propat = createPattern(pattern);
        String sequence1 = "MREI";
        assertTrue(propat.matchesSequence(sequence1));
        String badSeqeunce = "AMREI"; // not a match, needs to be at the end
        assertFalse(propat.matchesSequence(badSeqeunce));
    }

    /**
     * I_CONOTOXIN; PS60019;; C-{C}(6)-C-{C}(5)-C-C-x(1,3)-C-C-x(2,4)-C-x(3,10)-C.]
     * Example
     * >CI2_CONVR/29-55 : PS60019
     * CFPPGIYCTPYLPCCwgiCCgt..Crnv.......C
     * CFPPGIYCTPYLPCCWGICCGTCRNVC
     */
    @Test
    public void testI_CONOTOXIN() {
        String pattern = "C-{C}(6)-C-{C}(5)-C-C-x(1,3)-C-C-x(2,4)-C-x(3,10)-C";
        PrositePattern propat = createPattern(pattern);
        String sequence1 = "CFPPGIYCTPYLPCCWGICCGTCRNVC";
        assertTrue(propat.matchesSequence(sequence1));
        String badSequence1 = "CFCPGIYCTPYLPCCWGICCGTCRNVC";
        assertFalse(propat.matchesSequence(badSequence1));
    }

    /**
     * ID: RTC; AC: PS01287; PA: [RH]-G-x(2)-P-x-G(3)-x-[LIV]
     * Here are some examples  RGmpPgGGGeV, RGapPnGGGsV, HGfyPaGGGvV, RGyyPkGGGeV
     */
    @Test
    public void testCorrectParseOfMultilinePattern() {

    }

}
