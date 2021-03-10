package org.jax.isopret.interpro;

import org.jax.isopret.except.IsopretRuntimeException;
import org.jax.isopret.transcript.AccessionNumber;

import java.util.Optional;



public class InterproAnnotation {

    private final AccessionNumber enst;
    private final AccessionNumber ensg ;
    private final int interpro;
    private final int start;
    private final int end;


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
