package org.jax.isopret.data;

import org.jax.isopret.exception.IsopretRuntimeException;

import java.util.Objects;
import java.util.Optional;


/**
 * This class is used to help display Interpro protein domain annotations. It is a superclass
 * for  DisplayInterproAnnotation. TODO IMPROVE DOCUMENTATION
 */
public class InterproAnnotation {
    /**
     * Accession number of the transcript that corresponds to the protein isoform being annotated.
     */
    protected final AccessionNumber enst;
    /**
     * Accession number of the gene that corresponds to the protein being annotated.
     */
    protected final AccessionNumber ensg ;
    /**
     * Integer representation of the Interpro annotation (e.g., IPR004939 would be represented as 4939)
     */
    protected final int interpro;
    /**
     * Start residue position of the interpro domain in the protein
     */
    protected final int start;
    /**
     * End residue position of the interpro domain in the protein
     */
    protected final int end;


    public InterproAnnotation(AccessionNumber enst, AccessionNumber ensg, int interpro, int start, int end) {
        this.enst = enst;
        this.ensg = ensg;
        this.interpro = interpro;
        this.start = start;
        this.end = end;
    }

    public InterproAnnotation(InterproAnnotation that) {
        this.enst = that.enst;
        this.ensg = that.ensg;
        this.interpro = that.interpro;
        this.start = that.start;
        this.end = that.end;
    }


    public AccessionNumber getEnst() {
        return enst;
    }

    public AccessionNumber getEnsg() {
        return ensg;
    }

    public int getInterpro() {
        return interpro;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public int getLength() { return end - start + 1; }

    @Override
    public int hashCode() {
        return Objects.hash(this.enst, this.ensg, this.interpro, this.start, this.end);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (! (obj instanceof InterproAnnotation that)) return false;
        return this.ensg.equals(that.ensg) &&
                this.enst.equals(that.enst) &&
                this.interpro == that.interpro &&
                this.start == that.start &&
                this.end == that.end;
    }


    public static Optional<InterproAnnotation> fromLine(String line) {
        String [] fields = line.split("\t");
        if (fields.length != 5) {
            throw new IsopretRuntimeException("We were expecting 5 fields but got " + fields.length + " for \"" + line +"\"");
        }
        AccessionNumber enst = AccessionNumber.ensemblTranscript(fields[0]);
        AccessionNumber ensg = AccessionNumber.ensemblGene(fields[1]);
        String interproId = fields[2];
        if (interproId == null || interproId.isEmpty() ) {
            return Optional.empty();
        }
        int interpro = InterproEntry.integerPart(interproId);
        int start = Integer.parseInt(fields[3]);
        int end = Integer.parseInt(fields[4]);
        return Optional.of(new InterproAnnotation(enst, ensg, interpro, start, end));
    }
}
