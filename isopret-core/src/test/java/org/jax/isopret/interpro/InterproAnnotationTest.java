package org.jax.isopret.interpro;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class InterproAnnotationTest extends InterproTestBase{

    @Test
    void testLine() {
        assertEquals(641515, ipr000276annotation.getEnst().getAccessionNumber());
        assertEquals(186092, ipr000276annotation.getEnsg().getAccessionNumber());
        assertEquals(276, ipr000276annotation.getInterpro());
        assertEquals(40, ipr000276annotation.getStart());
        assertEquals(64, ipr000276annotation.getEnd());
    }
}
