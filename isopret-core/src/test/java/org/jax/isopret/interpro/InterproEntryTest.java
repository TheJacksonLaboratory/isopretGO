package org.jax.isopret.interpro;


import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.jax.isopret.data.InterproEntry;
import static org.jax.isopret.data.InterproEntryType.ACTIVE_SITE;


/**
 * This simple test class tests the construction of the {@link InterproEntry}
 * that is constructed in {@link InterproTestBase}.
 */
public class InterproEntryTest extends InterproTestBase {


    @Test
    public void testId() {
        assertEquals(138, IPR000138.getId());
    }
    @Test
    public void testAccession() {
        assertEquals("IPR000138", IPR000138.getIntroproAccession());
    }

    @Test
    public void testType() {
        assertEquals(ACTIVE_SITE, IPR000138.getEntryType());
    }

    @Test
    public void testDescription() {
        assertEquals("Hydroxymethylglutaryl-CoA lyase, active site", IPR000138.getDescription());
    }



}
