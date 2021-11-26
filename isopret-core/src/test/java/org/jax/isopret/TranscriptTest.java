package test.java.org.jax.isopret;

import org.jax.core.hbadeals.HbaDealsResult;
import org.jax.core.interpro.DisplayInterproAnnotation;
import org.jax.core.prosite.PrositeHit;
import org.jax.core.prosite.PrositeMapParser;
import org.jax.core.prosite.PrositeMapping;
import org.jax.core.transcript.AccessionNumber;
import org.jax.core.transcript.AnnotatedGene;
import org.jax.core.transcript.Transcript;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class TranscriptTest extends TestBase {
    /** Initialized in TestBase -- the key is ADAR, and there is a list of transcripts. */
    private static final Map<String, List<Transcript>> symbolToTranscriptMap = getADARToTranscriptMap();



    @Test
    public void if_ADAR_transcripts_found_then_ok() {
        assertTrue(symbolToTranscriptMap.containsKey("ADAR"));
    }

    @Test
    public void if_10_transcripts_retrieved_then_ok() {
        List<Transcript> adarTranscripts = symbolToTranscriptMap.get("ADAR");
        assertEquals(10, adarTranscripts.size());
        for (var t : adarTranscripts) {
            System.out.println(t);
        }
    }


    /**
     * Set up test for ENST00000368474.8.
     * Note that the Prosite map does not have a version number
     * TODO -- figure out what to do with version numbers
     */
    @Test
    public void testMapToTranscript() {
        List<Transcript> adarTranscripts = symbolToTranscriptMap.get("ADAR");
        PrositeMapParser prositeMapParser = getPrositeMapParser();
        // Get the relevant transcript
        AccessionNumber transcriptIDWithVersion = AccessionNumber.ensemblTranscript("ENST00000368474.8");
        String transcriptIDWithoutVersion = "ENST00000368474";
        String adarGeneId = "ENSG00000160710";
        AccessionNumber adarGeneAccession = AccessionNumber.ensemblGene(adarGeneId);
        Transcript enst00000368474 = adarTranscripts
                .stream()
                .filter(t->t.accessionId().equals(transcriptIDWithVersion))
                .findAny()
                .get();
        assertNotNull(enst00000368474);
        // get the relevant prosite mappings
        Map<AccessionNumber, PrositeMapping> prositeMappingMap = prositeMapParser.getPrositeMappingMap();
        assertTrue(prositeMappingMap.containsKey(adarGeneAccession));
        PrositeMapping enst00000368474Prosite = prositeMappingMap.get(adarGeneAccession);
        List<PrositeHit> hits = enst00000368474Prosite.getHits(transcriptIDWithoutVersion);
        // This transcript has 6 prosite motifs in our test file
        assertEquals(6, hits.size());
        // TODO -- starting here we should prototype how to map between the exon positions and the prosite motifs
        Map<String, HbaDealsResult> hbadealmaps = getADARHbaDealsResultMap();
        assertTrue(hbadealmaps.containsKey("ADAR"));
        HbaDealsResult adarResult = hbadealmaps.get("ADAR"); // expressed genes

        PrositeMapping psm = prositeMappingMap.get(adarGeneId);
        Map<AccessionNumber, List<DisplayInterproAnnotation>> annotList = Map.of(); // TODO
        AnnotatedGene atranscript = new AnnotatedGene(adarTranscripts, annotList, adarResult);
        assertEquals(atranscript.getSymbol(), "ADAR");
    }

}
