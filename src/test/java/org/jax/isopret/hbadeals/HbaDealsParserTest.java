package org.jax.isopret.hbadeals;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HbaDealsParserTest {
    private static final Path HBADEALS_ADAR_PATH = Paths.get("src/test/resources/hbadeals/ADAR_HBADEALS.tsv");
    private static final HbaDealsParser parser = new HbaDealsParser(HBADEALS_ADAR_PATH.toString());
    private static final Map<String, HbaDealsResult> hbaDealsResultMap = parser.getHbaDealsResultMap();

    @Test
    public void if_hbadeals_adar_results_retrieved_then_ok() {
        assertTrue(hbaDealsResultMap.containsKey("ADAR"));
    }

    /**
     * We expect five transcript results and one expression result
     */
    @Test
    public void if_five_hbadeals_adar_transcript_results_retrieved_then_ok() {
        HbaDealsResult adar = hbaDealsResultMap.get("ADAR");
        assertEquals(5, adar.getTranscriptMap().size());
    }




}
