package org.jax.isopret.prosite;

import java.util.Objects;

public class PrositeHit {

    private final String accession;
    private final int startAminoAcidPos;
    private final int endAminoAcidPos;

    public PrositeHit(String ac, int begin, int end) {
        this.accession = ac;
        this.startAminoAcidPos = begin;
        this.endAminoAcidPos = end;
    }

    @Override
    public int hashCode() {
        return Objects.hash(accession, startAminoAcidPos, endAminoAcidPos);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (! (obj instanceof PrositeHit)) return false;
        PrositeHit that = (PrositeHit) obj;
        return this.accession.equals(that.accession)
                && this.startAminoAcidPos == that.startAminoAcidPos &&
                this.endAminoAcidPos == that.endAminoAcidPos;
    }
}
