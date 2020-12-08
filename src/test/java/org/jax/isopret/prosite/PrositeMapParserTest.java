package org.jax.isopret.prosite;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

public class PrositeMapParserTest {
    private static final Path PROSITE_MAP_PATH = Paths.get("ssrc/test/resources/prosite/ADAR_prosite_profiles.txt");
    private static final Path PROSITE_DAT_PATH = Paths.get("src/test/resources/prosite/prosite-excerpt.dat");

    @Test
    public void testPrositeMapParser() {
        PrositeMapParser parser = new PrositeMapParser(PROSITE_MAP_PATH.toString(), PROSITE_DAT_PATH.toString());

    }


}
