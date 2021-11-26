package org.jax.isopret.ensembl;

import org.jax.isopret.core.ensembl.EnsemblTranscript;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EnsemblTranscriptTest {

    private static final String header = "ENST00000474740.1 cdna chromosome:GRCh38:18:9126627:9134295:1 gene:ENSG00000178127.13 gene_biotype:protein_coding transcript_biotype:processed_transcript gene_symbol:NDUFV2 description:NADH:ubiquinone oxidoreductase core subunit V2 [Source:HGNC Symbol;Acc:HGNC:7717]";
    private static final String seq = "ACGT";
    private static final EnsemblTranscript ndufv2 = new EnsemblTranscript(header, seq);

    @Test
    public void testNDUFV2() {
        assertEquals("ENST00000474740", ndufv2.getTranscriptId());
        assertEquals(1, ndufv2.getTranscriptVersion());
        assertEquals("cdna", ndufv2.getSeqtype());
        assertEquals("GRCh38:18:9126627:9134295:1", ndufv2.getChromosomalLocation());
        assertEquals("ENSG00000178127", ndufv2.getGeneId());
        assertEquals(13, ndufv2.getGeneVersion());
        assertEquals("protein_coding", ndufv2.getGeneBiotype());
        assertEquals("processed_transcript", ndufv2.getTranscriptBiotype());
        assertEquals("NDUFV2", ndufv2.getGeneSymbol());
        assertEquals("NADH:ubiquinone oxidoreductase core subunit V2 [Source:HGNC Symbol;Acc:HGNC:7717]", ndufv2.getDescription());
    }


}
