package org.jax.isopret.core.impl.rnaseqdata;

import org.jax.isopret.data.AccessionNumber;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RnaSeqResultLineTest {

    private static final String geneLine = "ENSG00000283674	Expression	-0.00850565803016342	0.98466";
    private static final String transcriptLine = "ENSG00000283674	ENST00000635775	0.955327995860536	0.96543";

    private static final double EPSILON = 0.0000000001;
    @Test
    public void testGeneLine() {
        RnaSeqResultLine line = RnaSeqResultLine.fromEnsembl(geneLine);
        AccessionNumber accessionNumber = AccessionNumber.ensemblGene("ENSG00000283674");
        assertEquals(accessionNumber, line.geneAccession());
        assertFalse(line.isIsoform());
        assertNull(line.isoform());
        assertEquals(-0.00850565803016342, line.expFC(), EPSILON);
        assertEquals(0.98466, line.raw_p(), EPSILON);
    }

    @Test
    public void testTranscriptLine() {
        RnaSeqResultLine line = RnaSeqResultLine.fromEnsembl(transcriptLine);
        AccessionNumber accessionNumber = AccessionNumber.ensemblGene("ENSG00000283674");
        AccessionNumber transcriptAcc = AccessionNumber.ensemblTranscript("ENST00000635775");
        assertEquals(accessionNumber, line.geneAccession());
        assertTrue(line.isIsoform());
        assertEquals(transcriptAcc, line.isoform());
        assertEquals(0.955327995860536, line.expFC(), EPSILON);
        assertEquals(0.96543, line.raw_p(), EPSILON);
    }



}
