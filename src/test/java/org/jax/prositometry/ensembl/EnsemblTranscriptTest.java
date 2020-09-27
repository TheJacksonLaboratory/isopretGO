package org.jax.prositometry.ensembl;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EnsemblTranscriptTest {


    @Test
    public void testNDUFV2() {
        String header = "ENST00000474740.1 cdna chromosome:GRCh38:18:9126627:9134295:1 gene:ENSG00000178127.13 gene_biotype:protein_coding transcript_biotype:processed_transcript gene_symbol:NDUFV2 description:NADH:ubiquinone oxidoreductase core subunit V2 [Source:HGNC Symbol;Acc:HGNC:7717]";
        String seq = "ACGT";
        EnsemblTranscript transcript = new EnsemblTranscript(header, seq);
        assertEquals("ENST00000474740", transcript.getTranscriptId());
        assertEquals(1, transcript.getTranscriptVersion());
        assertEquals("cdna", transcript.getSeqtype());
        assertEquals("GRCh38:18:9126627:9134295:1", transcript.getChromosomalLocation());
        assertEquals("ENSG00000178127", transcript.getGeneId());
        assertEquals(13, transcript.getGeneVersion());
        assertEquals("protein_coding", transcript.getGeneBiotype());
        assertEquals("processed_transcript", transcript.getTranscriptBiotype());
        assertEquals("NDUFV2", transcript.getGeneSymbol());
        assertEquals("NADH:ubiquinone oxidoreductase core subunit V2 [Source:HGNC Symbol;Acc:HGNC:7717]", transcript.getDescription());
    }


}
