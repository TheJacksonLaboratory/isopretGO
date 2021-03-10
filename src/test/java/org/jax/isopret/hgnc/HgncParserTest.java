package org.jax.isopret.hgnc;

import org.jax.isopret.TestBase;
import java.util.Map;

import org.jax.isopret.transcript.AccessionNumber;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The hgncParser is initialized in {@link TestBase}
 *
 */
public class HgncParserTest extends TestBase {

    @Test
    public void if_parser_retrieves_six_items_ok() {
        assertEquals(6, hgncParser.itemCount());
    }

    @Test
    public void testEnsemblMap() {
        Map<AccessionNumber, HgncItem> ensemblMap = hgncParser.ensemblMap();
        assertEquals(6, ensemblMap.size());
        for (AccessionNumber ens : ensemblMap.keySet()) {
            assertTrue(ens.getAccessionString().startsWith("ENSG"));
        }
        assertTrue(ensemblMap.containsKey(AccessionNumber.ensemblGene("ENSG00000121410")));
        assertTrue(ensemblMap.containsKey(AccessionNumber.ensemblGene("ENSG00000268895")));
        assertTrue(ensemblMap.containsKey(AccessionNumber.ensemblGene("ENSG00000166147")));
        assertTrue(ensemblMap.containsKey(AccessionNumber.ensemblGene("ENSG00000177519")));
        assertTrue(ensemblMap.containsKey(AccessionNumber.ensemblGene("ENSG00000124614")));
        assertTrue(ensemblMap.containsKey(AccessionNumber.ensemblGene("ENSG00000160710")));

        // test ENSG00000121410
        HgncItem item = ensemblMap.get(AccessionNumber.ensemblGene("ENSG00000121410"));
        assertEquals("A1BG", item.getGeneSymbol());
        assertEquals("alpha-1-B glycoprotein", item.getGeneName());
        assertEquals("1", item.getEntrezId());
        assertEquals("NM_130786", item.getRefseqAccecssion());
        assertEquals("uc002qsd.5", item.getUcscId());
        assertEquals("ENSG00000121410", item.getEnsemblGeneId());
    }

//    @Test
//    public void testUcscMap() {
//        Map<String, HgncItem> ucscMap = hgncParser.ucscMap();
//        assertEquals(6, ucscMap.size());
//        for (String ens : ucscMap.keySet()) {
//            assertTrue(ens.startsWith("uc"));
//        }
//        assertTrue(ucscMap.containsKey("uc002qsd.5"));
//        assertTrue(ucscMap.containsKey("uc002qse.3"));
//        assertTrue(ucscMap.containsKey("uc001zwx.3"));
//        assertTrue(ucscMap.containsKey("uc002tyq.1"));
//        assertTrue(ucscMap.containsKey("uc003ojn.3"));
//        assertTrue(ucscMap.containsKey("uc001ffh.4"));
//        HgncItem fbn1 = ucscMap.get("uc001zwx.3");
//        assertEquals("FBN1", fbn1.getGeneSymbol());
//    }

//    @Test
//    public void testRefseqMap() {
//        Map<String, HgncItem> refseqMap = hgncParser.refseqMap();
//        // one of the items does not have an NM accession number, soo we only get 5 of 6 items in this map
//        assertEquals(5, refseqMap.size());
//        for (String refseq : refseqMap.keySet()) {
//            System.out.println(refseq);
//            assertTrue(refseq.startsWith("NM_"));
//        }
//        assertTrue(refseqMap.containsKey("NM_019845"));
//        assertTrue(refseqMap.containsKey("NM_130786"));
//        assertTrue(refseqMap.containsKey("NM_000138"));
//        assertTrue(refseqMap.containsKey("NM_001014"));
//        assertTrue(refseqMap.containsKey("NM_001111"));
//
//    }
}
