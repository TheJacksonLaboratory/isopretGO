package org.jax.isopret.hgnc;

import org.jax.isopret.core.model.GeneModel;
import org.jax.isopret.TestBase;
import java.util.Map;

import org.jax.isopret.core.model.AccessionNumber;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The hgncParser is initialized in {@link TestBase}
 *
 */
public class HgncParserTest extends TestBase {



    @Test
    public void testEnsemblMap() {
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
        GeneModel item = ensemblMap.get(AccessionNumber.ensemblGene("ENSG00000121410"));
        assertEquals("A1BG", item.geneSymbol());
        assertEquals("alpha-1-B glycoprotein", item.geneName());
        assertEquals("1", item.entrezId());
        assertEquals("NM_130786", item.refseqAccecssion());
        AccessionNumber a1bgAcc = AccessionNumber.ensemblGene("ENSG00000121410");
        assertEquals(a1bgAcc, item.ensemblGeneId());
    }


}
