package org.jax.isopret.interpro;

import org.jax.isopret.core.interpro.DisplayInterproAnnotation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DisplayInterproAnnotationTest extends InterproTestBase{

    private static final DisplayInterproAnnotation displayInterproAnnotation =
            new DisplayInterproAnnotation(ipr000276annotation, IPR000276);
    private static final DisplayInterproAnnotation displayInterproAnnotationShiftedRight =
            new DisplayInterproAnnotation(ipr000276annotationShiftedRight, IPR000276);
    private static final DisplayInterproAnnotation displayInterproAnnotationShiftedLeft =
            new DisplayInterproAnnotation(ipr000276annotationSHiftedLeft, IPR000276);
    private static final DisplayInterproAnnotation displayInterproAnnotationIncluded =
            new DisplayInterproAnnotation(ipr000276annotationIncluded, IPR000276);
    private static final DisplayInterproAnnotation displayInterproAnnotationComprises =
            new DisplayInterproAnnotation(ipr000276annotationComprises, IPR000276);


    /**
     * The annotation is to a family element
     */
   @Test
    public void testIsFamily() {
       assertTrue(displayInterproAnnotation.isFamily());
   }


   @Test void testMergeWithShiftedRight() {
      assertEquals(40, displayInterproAnnotation.getStart());
       assertEquals(64, displayInterproAnnotation.getEnd());
       assertEquals(42, displayInterproAnnotationShiftedRight.getStart());
       assertEquals(70, displayInterproAnnotationShiftedRight.getEnd());
       // we merge and expect to get 40-70
       DisplayInterproAnnotation merged = displayInterproAnnotation.merge(displayInterproAnnotationShiftedRight);
       assertEquals(40, merged.getStart());
       assertEquals(70, merged.getEnd());
       assertTrue(merged.isFamily());
   }


    @Test void testMergeWithShiftedLeft() {
        assertEquals(40, displayInterproAnnotation.getStart());
        assertEquals(64, displayInterproAnnotation.getEnd());
        assertEquals(27, displayInterproAnnotationShiftedLeft.getStart());
        assertEquals(52, displayInterproAnnotationShiftedLeft.getEnd());
        // we merge and expect to get 27-64
        DisplayInterproAnnotation merged = displayInterproAnnotation.merge(displayInterproAnnotationShiftedLeft);
        assertEquals(27, merged.getStart());
        assertEquals(64, merged.getEnd());
        assertTrue(merged.isFamily());
    }

    @Test void testMergeWithIncluded() {
        assertEquals(40, displayInterproAnnotation.getStart());
        assertEquals(64, displayInterproAnnotation.getEnd());
        assertEquals(45, displayInterproAnnotationIncluded.getStart());
        assertEquals(60, displayInterproAnnotationIncluded.getEnd());
        // we merge and expect to get 40-64
        DisplayInterproAnnotation merged = displayInterproAnnotation.merge(displayInterproAnnotationIncluded);
        assertEquals(40, merged.getStart());
        assertEquals(64, merged.getEnd());
        assertTrue(merged.isFamily());
    }
    @Test void testMergeWithComprises() {
        assertEquals(40, displayInterproAnnotation.getStart());
        assertEquals(64, displayInterproAnnotation.getEnd());
        assertEquals(30, displayInterproAnnotationComprises.getStart());
        assertEquals(80, displayInterproAnnotationComprises.getEnd());
        // we merge and expect to get 40-64
        DisplayInterproAnnotation merged = displayInterproAnnotation.merge(displayInterproAnnotationComprises);
        assertEquals(30, merged.getStart());
        assertEquals(80, merged.getEnd());
        assertTrue(merged.isFamily());
    }

}
