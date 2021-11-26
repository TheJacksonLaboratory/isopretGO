package org.jax.isopret.interpro;

import org.jax.isopret.core.interpro.InterproAnnotation;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class InterproAnnotationTest {

    @Test
    void testLine() {
        String line = "ENST00000641515	ENSG00000186092	IPR000276	40	64";
        Optional<InterproAnnotation> opt = InterproAnnotation.fromLine(line);
        assertTrue(opt.isPresent());
        InterproAnnotation annot = opt.get();
        assertEquals(641515, annot.getEnst().getAccessionNumber());
        assertEquals(186092, annot.getEnsg().getAccessionNumber());
        assertEquals(276, annot.getInterpro());
        assertEquals(40, annot.getStart());
        assertEquals(64, annot.getEnd());
    }
}
