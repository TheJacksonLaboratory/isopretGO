package org.jax.isopret.model;

import org.jax.isopret.core.impl.rnaseqdata.TranscriptResultImpl;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TranscriptResultTest {

    private static final AccessionNumber transcriptAccession = AccessionNumber.ensemblTranscript("ENST00000635775");
    private static final double foldChange = 2.7;
    private static final double pval = 0.001;

    private static final double EPSILON = 0.0000000001;

    private static final TranscriptResult result = new TranscriptResultImpl(transcriptAccession, foldChange, pval);

    @Test
    public void testAccession() {
        assertEquals(transcriptAccession, result.getTranscriptId());
    }

    @Test
    public void testFoldchange() {
        assertEquals(pval, result.getPvalue(), EPSILON);
    }
}
