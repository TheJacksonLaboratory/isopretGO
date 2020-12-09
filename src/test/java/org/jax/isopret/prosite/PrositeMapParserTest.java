package org.jax.isopret.prosite;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PrositeMapParserTest {
    private static final Path PROSITE_MAP_PATH = Paths.get("src/test/resources/prosite/ADAR_prosite_profiles.txt");
    private static final Path PROSITE_DAT_PATH = Paths.get("src/test/resources/prosite/prosite-excerpt.dat");

    /**
     * Test that we correctly extract a map from Prosite accession numbers such as
     * PS50141 to the corresponding motif/domain names, such as A_DEAMIN_EDITASE
     */
    @Test
    public void testPrositeMapParser() {
        System.out.println(PROSITE_MAP_PATH);
        PrositeMapParser parser = new PrositeMapParser(PROSITE_MAP_PATH.toString(), PROSITE_DAT_PATH.toString());
        Map<String,String> nameMap = parser.getPrositeNameMap();
        assertTrue(nameMap.containsKey("PS50141"));
        assertEquals("A_DEAMIN_EDITASE", nameMap.get("PS50141"));
        assertTrue(nameMap.containsKey("PS00001"));
        assertEquals("ASN_GLYCOSYLATION", nameMap.get("PS00001"));
        assertTrue(nameMap.containsKey("PS00004"));
        assertEquals("CAMP_PHOSPHO_SITE", nameMap.get("PS00004"));
        assertTrue(nameMap.containsKey("PS50137"));
        assertEquals("DS_RBD", nameMap.get("PS50137"));
        assertTrue(nameMap.containsKey("PS50139"));
        assertEquals("Z_BINDING", nameMap.get("PS50139"));
    }


}
