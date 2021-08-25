package org.jax.isopret.interpro;

public class InterproDomain {

    private final int enst;
    private final int ensg;
    private final int interpro;
    private final int start;
    private final int end;

    public InterproDomain(int enst, int ensg, int interpro, int start, int end) {
        this.enst = enst;
        this.ensg = ensg;
        this.interpro = interpro;
        this.start = start;
        this.end = end;
    }

    public int getEnst() {
        return enst;
    }

    public int getEnsg() {
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
}