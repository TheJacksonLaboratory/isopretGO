package org.jax.isopret.interpro;

import org.junit.jupiter.api.Test;


import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class InterproDomainDescParserTest {
    private static final Path DOMAIN_DESC = Paths.get("src/test/resources/interpro/interpro_domain_desc_small.txt");

    private static final Map<Integer, InterproEntry> interproEntryMap =
            InterproDomainDescParser.getInterproDescriptionMap(DOMAIN_DESC.toFile());


    @Test
    public void testExtract13entries() {
        int expectedEntryCount = 13;
        assertEquals(expectedEntryCount, interproEntryMap.size());
    }

    /**
     * 126	Active_site	Serine proteases, V8 family, serine active site
     *
     */
    @Test
    public void testEntryType126() {
        InterproEntry entry126 = interproEntryMap.get(126);
        assertNotNull(entry126);
        assertEquals(InterproEntryType.ACTIVE_SITE, entry126.getEntryType());
        assertEquals(126, entry126.getId());
        assertEquals("IPR000126", entry126.getIntroproAccession());
        assertEquals("Serine proteases, V8 family, serine active site", entry126.getDescription());
        assertFalse(entry126.isFamilyOrSuperfamily());
    }

    /**
     * IPR000607	Repeat	Double-stranded RNA-specific adenosine deaminase (DRADA) repeat
     */
    @Test
    public void testEntryType607() {
        InterproEntry entry607 = interproEntryMap.get(607);
        assertNotNull(entry607);
        assertEquals(InterproEntryType.REPEAT, entry607.getEntryType());
        assertEquals(607, entry607.getId());
        assertEquals("IPR000607", entry607.getIntroproAccession());
        assertEquals("Double-stranded RNA-specific adenosine deaminase (DRADA) repeat", entry607.getDescription());
        assertFalse(entry607.isFamilyOrSuperfamily());
    }

    /**
     * IPR014720	Domain	Double-stranded RNA-binding domain
     */
    @Test
    public void testEntryType14720() {
        InterproEntry entry14720 = interproEntryMap.get(14720);
        assertNotNull(entry14720);
        assertEquals(InterproEntryType.DOMAIN, entry14720.getEntryType());
        assertEquals(14720, entry14720.getId());
        assertEquals("IPR014720", entry14720.getIntroproAccession());
        assertEquals("Double-stranded RNA-binding domain", entry14720.getDescription());
        assertFalse(entry14720.isFamilyOrSuperfamily());
    }




    /**
     * IPR036388	Homologous_superfamily	Winged helix-like DNA-binding domain superfamily
     */
    @Test
    public void testEntryType36388() {
        InterproEntry entry36388 = interproEntryMap.get(36388);
        assertNotNull(entry36388);
        assertEquals(InterproEntryType.HOMOLOGOUS_SUPERFAMILY, entry36388.getEntryType());
        assertEquals(36388, entry36388.getId());
        assertEquals("IPR036388", entry36388.getIntroproAccession());
        assertEquals("Winged helix-like DNA-binding domain superfamily", entry36388.getDescription());
        assertTrue(entry36388.isFamilyOrSuperfamily());
    }
}
