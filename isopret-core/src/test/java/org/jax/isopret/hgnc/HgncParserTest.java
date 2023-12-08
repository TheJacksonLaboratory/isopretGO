package org.jax.isopret.hgnc;

import org.jax.isopret.model.GeneModel;
import org.jax.isopret.TestBase;

import org.jax.isopret.data.AccessionNumber;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The hgncParser is initialized in {@link TestBase}
 *
 */
public class HgncParserTest extends TestBase {

    private final AccessionNumber adarEnsembl = AccessionNumber.ensemblGene("ENSG00000160710");
    /**
     * Note that isopret only retains gene models for which we have both HGNC and Jannovar data.
     * In our case, this means that we only retain data for ADAR.
     */
    @Test
    public void testEnsemblMap() {
        assertEquals(1, ensemblMap.size());
        for (AccessionNumber ens : ensemblMap.keySet()) {
            assertTrue(ens.getAccessionString().startsWith("ENSG"));
        }
        assertTrue(ensemblMap.containsKey(adarEnsembl));
        // test ENSG00000121410
        GeneModel item = ensemblMap.get(adarEnsembl);
        assertEquals("ADAR", item.geneSymbol());
        assertEquals("adenosine deaminase RNA specific", item.geneName());
        assertEquals("103", item.entrezId());
        assertEquals("NM_001111", item.refseqAccecssion());
    }


}
