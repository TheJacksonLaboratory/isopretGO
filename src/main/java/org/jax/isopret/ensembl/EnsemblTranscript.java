package org.jax.isopret.ensembl;

import org.jax.isopret.except.IsopretRuntimeException;
import org.jax.isopret.orf.OrfFinder;
import org.jax.isopret.transcript.AccessionNumber;

/**
 * Extract information from the header line, which has the following format. Note that we can
 * split on space for everything before the description element.
 * >ENST00000390372.3 cdna chromosome:GRCh38:7:142482548:142483019:1 gene:ENSG00000211725.3 gene_biotype:TR_V_gene
 * transcript_biotype:TR_V_gene gene_symbol:TRBV5-5
 * description:T cell receptor beta variable 5-5 [Source:HGNC Symbol;Acc:HGNC:12222]
 */
public class EnsemblTranscript {

    private final String description;
    /** Ensembl transcript id without version number. */
    private final String transcriptId;
    private final int transcriptVersion;
    /** intger representation of transcript id (without version): 560355for ENST00000560355.1 */
    private final AccessionNumber acccession;
    private final String geneId;
    private final int geneVersion;

    private final String seqtype;
    private final String chromosomalLocation;

    private final String geneBiotype;
    private final String transcriptBiotype;
    private final String geneSymbol;
    private final String cDNA;
    private final String longestAaSequence;
    private final boolean hasOrf;


    public EnsemblTranscript(String header, String sequence) {
        String descr = "n/a";
        int i = header.indexOf("description:");
        if (i > 0) {
            int j = i+12;
            descr = header.substring(j);
            header = header.substring(0,j).trim();
        }
        this.description = descr;
        String [] fields = header.split("\\s+");
        String transcript_id = fields[0];
        if (! transcript_id.startsWith("ENST")) {
            throw new IsopretRuntimeException("Malformed Ensembl transcript id: " + transcript_id);
        }
        this.acccession = AccessionNumber.ensemblTranscript(fields[0]);
        i = transcript_id.indexOf(".");
        if (i > 0) {
            this.transcriptId = transcript_id.substring(0,i); // removes the version number
            String id = transcript_id.substring(i+1);
            this.transcriptVersion = Integer.parseInt(id);
        } else {
            this.transcriptId = transcript_id;
            this.transcriptVersion = -1; // should never happen!
        }
        seqtype = fields[1];
        String chromString = fields[2];
        if (! chromString.startsWith("chromosome") && ! chromString.startsWith("scaffold")) {
            throw new IsopretRuntimeException("Malformed chromosome string: " + chromString);
        }
        this.chromosomalLocation = chromString.substring(11);
        String geneString = fields[3];
        if (! geneString.startsWith("gene:")) {
            throw new IsopretRuntimeException("Malformed gene string: " + geneString);
        }
        geneString = geneString.substring(5);
        i = geneString.indexOf(".");
        if (i > 0) {
            this.geneId = geneString.substring(0,i); // removes the version number
            String id = geneString.substring(i+1);
            this.geneVersion = Integer.parseInt(id);
        } else {
            this.geneId = geneString;
            this.geneVersion = -1; // should never happen!
        }
        String geneBiot = fields[4];
        if (! geneBiot.startsWith("gene_biotype")) {
            throw new IsopretRuntimeException("Malformed gene_biotype string: " + geneBiot);
        }
        this.geneBiotype = geneBiot.substring(13);
        String trBiot = fields[5];
        if (! trBiot.startsWith("transcript_biotype")) {
            throw new IsopretRuntimeException("Malformed transcript_biotype string: " + trBiot);
        }
        this.transcriptBiotype = trBiot.substring(19);
        String sym = fields[6];
        if (! sym.startsWith("gene_symbol")) {
            throw new IsopretRuntimeException("Malformed gene_symbol string: " + sym);
        }
        this.geneSymbol = sym.substring(12);
        this.cDNA = sequence;
        OrfFinder orff = new OrfFinder(sequence);
        if (orff.hasORF()) {
            this.longestAaSequence = orff.getLongestAaSequence();
            this.hasOrf = true;
        } else {
            this.longestAaSequence = "";
            this.hasOrf = false;
        }
    }

    public String getDescription() {
        return description;
    }

    public String getTranscriptId() {
        return transcriptId;
    }

    public int getEnstId() {
        return this.acccession.getAccessionNumber();
    }

    public int getTranscriptVersion() {
        return transcriptVersion;
    }

    public String getGeneId() {
        return geneId;
    }

    public int getGeneVersion() {
        return geneVersion;
    }

    public String getSeqtype() {
        return seqtype;
    }

    public String getChromosomalLocation() {
        return chromosomalLocation;
    }

    public String getGeneBiotype() {
        return geneBiotype;
    }

    public String getTranscriptBiotype() {
        return transcriptBiotype;
    }

    public String getGeneSymbol() {
        return geneSymbol;
    }

    public String getcDNA() {
        return cDNA;
    }

    public boolean hasOrf() {
        return hasOrf;
    }

    public String getLongestAaSequence() {
        return longestAaSequence;
    }

    public int cDNAlen() {
        return this.cDNA.length();
    }

    public int aaLen() {
        return this.longestAaSequence.length();
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.geneSymbol).append(": ").append(this.transcriptId);
        return sb.toString();
    }
}
