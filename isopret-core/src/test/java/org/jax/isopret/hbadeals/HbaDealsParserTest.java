package org.jax.isopret.hbadeals;

import org.jax.isopret.core.impl.rnaseqdata.RnaSeqResultsParser;
import org.jax.isopret.TestBase;
import org.jax.isopret.core.impl.hgnc.HgncParser;
import org.jax.isopret.core.impl.jannovar.JannovarReader;
import org.jax.isopret.data.AccessionNumber;
import org.jax.isopret.data.Transcript;
import org.jax.isopret.model.*;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HbaDealsParserTest extends TestBase {

    private static final Path HBADEALS_ADAR_PATH = Paths.get("src/test/resources/hbadeals/ADAR_HBADEALS.tsv");
    private static final JannovarReader reader = new JannovarReader(JANNOVAR_ADAR_PATH.toAbsolutePath().toString(), assembly);
    private static final Map<GeneSymbolAccession, List<Transcript>> transcriptListMap = reader.getGeneToTranscriptListMap();
   private static final HgncParser hgncParser = new HgncParser(new File(hgncPath), transcriptListMap);
    private static final  Map<AccessionNumber, GeneModel> ensemblMap = hgncParser.ensemblMap();
    private static final Map<AccessionNumber, GeneResult> hbaDealsResultMap =
            RnaSeqResultsParser.fromHbaDeals(HBADEALS_ADAR_PATH.toFile(), ensemblMap);
    private final double THRESHOLD = 0.05;
    @Test
    public void if_hbadeals_adar_results_retrieved_then_ok() {
        assertTrue(hbaDealsResultMap.containsKey(adarAccession));
    }

    /**
     * We expect five transcript results and one expression result
     */
    @Test
    public void if_five_hbadeals_adar_transcript_results_retrieved_then_ok() {
        GeneResult adar = hbaDealsResultMap.get(adarAccession);
        assertEquals(5, adar.getTranscriptMap().size());
    }

    @Test
    public void if_adar_has_significant_dge_then_ok() {
        GeneResult adar = hbaDealsResultMap.get(adarAccession);
        assertTrue(adar.hasDifferentialExpressionResult(THRESHOLD));
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
        final double PEP_THRESHOLD = 0.05;
        GeneResult adar = hbaDealsResultMap.get(adarAccession);
        assertTrue(adar.hasDifferentialSplicingResult(THRESHOLD));
        double pva = adar.getSmallestSplicingP();
        assertEquals(1e-05, pva, EPSILON);
        Map<AccessionNumber, TranscriptResult> transcriptMap = adar.getTranscriptMap();
        assertEquals(5, transcriptMap.size());
        AccessionNumber ENST00000368471 = AccessionNumber.ensemblTranscript("ENST00000368471");
        TranscriptResult ENST00000368471result = transcriptMap.get(ENST00000368471);
        assertTrue(ENST00000368471result.isSignificant(PEP_THRESHOLD));
        assertEquals(1e-05, ENST00000368471result.getPvalue(), EPSILON);
        var ENST00000368474 = AccessionNumber.ensemblTranscript("ENST00000368474");
        TranscriptResult ENST00000368474result = transcriptMap.get(ENST00000368474);
        assertEquals(1.45668870537677, ENST00000368474result.getFoldChange(), EPSILON);
        assertEquals(0.00192, ENST00000368474result.getPvalue(), EPSILON);
        //	ENST00000463920	0.84541220998081
        var ENST00000463920 = AccessionNumber.ensemblTranscript("ENST00000463920");
        TranscriptResult ENST00000463920result = transcriptMap.get(ENST00000463920);
        assertEquals(0.84541220998081, ENST00000463920result.getFoldChange(), EPSILON);
        assertEquals(0.7134, ENST00000463920result.getPvalue(), EPSILON);
        //ENST00000529168	1.05034162415497	0.9602
        var ENST00000529168 = AccessionNumber.ensemblTranscript("ENST00000529168");
        TranscriptResult ENST00000529168result = transcriptMap.get(ENST00000529168);
        assertEquals(1.05034162415497, ENST00000529168result.getFoldChange(), EPSILON);
        assertEquals(0.9602, ENST00000529168result.getPvalue(), EPSILON);
        // ENST00000649021	0.833370141719852	0.66569
        var ENST00000649021 = AccessionNumber.ensemblTranscript("ENST00000649021");
        TranscriptResult ENST00000649021result = transcriptMap.get(ENST00000649021);
        assertEquals(0.833370141719852, ENST00000649021result.getFoldChange(), EPSILON);
        assertEquals(0.66569, ENST00000649021result.getPvalue(), EPSILON);
    }


    /**
     * ENSG00000160710	Expression	1.54770825394965	0
     * Thus, the expression log 2 foldchange is 1.54
     * The expression fold change is 2**1.54=0.6301335462576204.
     */
    @Test
    public void testGeneFoldChange() {
        GeneResult adar = hbaDealsResultMap.get(adarAccession);
        final double EPSILON = 1E-10;
        double expressionlog2FC = adar.getExpressionLog2FoldChange();
        assertEquals(1.54770825394965, expressionlog2FC, EPSILON);
        double foldChange =  Math.pow(expressionlog2FC, 2.0);
        assertEquals(foldChange, adar.getExpressionFoldChange(), EPSILON);
    }


}
