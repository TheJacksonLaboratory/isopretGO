package org.jax.isopret.core.impl.rnaseqdata;

import org.jax.isopret.exception.IsopretRuntimeException;
import org.jax.isopret.data.AccessionNumber;


/**
 * With the exception of the header line, the HBA-DEALS and the edgeR results file (from our script to
 * analyze both DGE and DAS) are the same. This class is therefore used to parse lines from either
 * HBA-DEALS or edgeR. Note that the objects are only used for parsing and are not a part of the
 * analysis model. Each line will be transformed either to a {@link org.jax.isopret.model.GeneResult} or
 * a {@link org.jax.isopret.model.TranscriptResult}.
 * @param geneAccession ENSEMBL accession number for a gene, e.g., ENSG00000139618
 * @param isoform ENSEMBL accession number for a transcript (can be null), e.g., ENST00000560355.1
 * @param expFC expression fold change (from HBA-DEALS)
 * @param raw_p raw p-value (from HBA-DEALS)
 * @author Peter N Robinson
 */
record RnaSeqResultLine (AccessionNumber geneAccession,
                         AccessionNumber isoform,
                         double expFC,
                         double raw_p
                         ){
    /**
     * @param line an HBA-DEALS file with Ensem data
     * @return an {@link RnaSeqResultLine} object with Ensembl {@link AccessionNumber} object
     */
    static RnaSeqResultLine fromEnsembl(String line) {
        String [] fields = line.split("\t");
        if (fields.length != 4) {
            String msg = String.format("[ERROR] Malformed line with %d fields: %s\n", fields.length, line);
            throw new IsopretRuntimeException(msg);
        }
        AccessionNumber geneAcc = AccessionNumber.ensemblGene(fields[0]);
        AccessionNumber transcriptAcc = null;
        if (! fields[1].equalsIgnoreCase("Expression")) {
            transcriptAcc = AccessionNumber.ensemblTranscript(fields[1]);
        }
        double expFC = Double.parseDouble(fields[2]);
        double raw_p = Double.parseDouble(fields[3]);
        return new RnaSeqResultLine(geneAcc, transcriptAcc, expFC, raw_p);
    }

    public boolean isIsoform() {
        return isoform != null;
    }



}
