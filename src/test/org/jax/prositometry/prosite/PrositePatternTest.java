package org.jax.prositometry.prosite;

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


}
