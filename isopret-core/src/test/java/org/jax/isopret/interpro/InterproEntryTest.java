package org.jax.isopret.interpro;

import org.jax.isopret.core.impl.interpro.InterproEntry;
import org.junit.jupiter.api.Test;

import static org.jax.isopret.core.impl.interpro.InterproEntryType.ACTIVE_SITE;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
