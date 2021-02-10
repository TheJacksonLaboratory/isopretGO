package org.jax.isopret.hbadeals;

import org.jax.isopret.TestBase;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HbaDealsParserTest extends TestBase {

    private static final Path HBADEALS_ADAR_PATH = Paths.get("src/test/resources/hbadeals/ADAR_HBADEALS.tsv");
    private static final HbaDealsParser parser = new HbaDealsParser(HBADEALS_ADAR_PATH.toString(), hgncParser.ensemblMap());
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

    @Test
    public void if_adar_has_significant_dge_then_ok() {
        HbaDealsResult adar = hbaDealsResultMap.get("ADAR");
        assertTrue(adar.hasSignificantExpressionResult());
        double p = adar.getExpressionP();
        assertTrue(p<0.0000001); // represented as p=0 in our test file
    }

    /**
     * Two of the expressed transcripts are significant, three are not
     * ENSG00000160710	ADAR	ENST00000368471	0.563281823470453	1e-05
     * ENSG00000160710	ADAR	ENST00000368474	1.45668870537677	0.00192
     */
    @Test
    public void if_adar_has_significant_das_then_ok() {
        final double EPSILON = 0.0001;
        HbaDealsResult adar = hbaDealsResultMap.get("ADAR");
        assertTrue(adar.hasaSignificantSplicingResult());
        double pva = adar.getMostSignificantSplicingPval();
        assertEquals(1e-05, pva, EPSILON);
        Map<String, HbaDealsTranscriptResult> transcriptMap = adar.getTranscriptMap();
        assertEquals(5, transcriptMap.size());
        HbaDealsTranscriptResult ENST00000368471 = transcriptMap.get("ENST00000368471");
        assertTrue(ENST00000368471.isSignificant());
        assertEquals(1e-05, ENST00000368471.getP(), EPSILON);
        HbaDealsTranscriptResult ENST00000368474 = transcriptMap.get("ENST00000368474");
        assertEquals(1.45668870537677, ENST00000368474.getFoldChange(), EPSILON);
        assertEquals(0.00192, ENST00000368474.getP(), EPSILON);
        //	ENST00000463920	0.84541220998081
        HbaDealsTranscriptResult ENST00000463920 = transcriptMap.get("ENST00000463920");
        assertEquals(0.84541220998081, ENST00000463920.getFoldChange(), EPSILON);
        assertEquals(0.7134, ENST00000463920.getP(), EPSILON);
        //ENST00000529168	1.05034162415497	0.9602
        HbaDealsTranscriptResult ENST00000529168 = transcriptMap.get("ENST00000529168");
        assertEquals(1.05034162415497, ENST00000529168.getFoldChange(), EPSILON);
        assertEquals(0.9602, ENST00000529168.getP(), EPSILON);
        // ENST00000649021	0.833370141719852	0.66569
        HbaDealsTranscriptResult ENST00000649021 = transcriptMap.get("ENST00000649021");
        assertEquals(0.833370141719852, ENST00000649021.getFoldChange(), EPSILON);
        assertEquals(0.66569, ENST00000649021.getP(), EPSILON);
    }






}
