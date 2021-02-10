package org.jax.isopret.transcript;

import org.jax.isopret.except.IsopretRuntimeException;
import org.monarchinitiative.svart.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Transcript extends GenomicRegion {

//    protected final Contig contig;
//    protected final Strand strand;
//    protected final CoordinateSystem coordinateSystem;
//
//    protected final Position start;
//    protected final Position end;

    private final String accessionId;
    /** e.g., if {@link #accessionId} is ENST0000064021.6 then this would be ENST0000064021 */
    private final String accessionIdNoVersion;

    private final String hgvsSymbol;

    private final boolean isCoding;
    private final Position cdsStart;
    private final Position cdsEnd;
    private final List<GenomicRegion> exons;

    private final GenomicRegion txRegion;

    private Transcript(GenomicRegion txRegion,
                       String accessionId,
                       String hgvsSymbol,
                       boolean isCoding,
                       Position cdsStart,
                       Position cdsEnd,
                       List<GenomicRegion> exons) {
        this.txRegion =  txRegion;
        this.accessionId = accessionId;
        int i = this.accessionId.indexOf(".");
        if (i < 0) {
            this.accessionIdNoVersion = this.accessionId;
        } else {
            this.accessionIdNoVersion = this.accessionId.substring(0,i);
        }
        this.hgvsSymbol = hgvsSymbol;
        this.isCoding = isCoding;
        this.cdsStart = cdsStart;
        this.cdsEnd = cdsEnd;
        this.exons = exons;
    }

    public static Transcript of(Contig contig,
                                int start,
                                int end,
                                Strand strand,
                                CoordinateSystem coordinateSystem,
                                int cdsStart, int cdsEnd,
                                String accessionId,
                                String hgvsSymbol,
                                boolean isCoding,
                                List<GenomicRegion> exons) {
        GenomicRegion txRegion = PreciseGenomicRegion.of(contig, strand, coordinateSystem, Position.of(start), Position.of(end));
        return new Transcript(txRegion, accessionId, hgvsSymbol, isCoding, Position.of(cdsStart), Position.of(cdsEnd), exons);
    }

    public String accessionId() {
        return accessionId;
    }

    public String hgvsSymbol() {
        return hgvsSymbol;
    }

    public boolean isCoding() {
        return isCoding;
    }

    public GenomicRegion cdsRegion() {
        return GenomicRegion.zeroBased(contig, strand, cdsStart, cdsEnd);
    }

    public Position cdsStart() {
        return GenomicPosition.zeroBased(contig, strand, cdsStart);
    }

    public GenomicPosition cdsEnd() {
        return GenomicPosition.zeroBased(contig, strand, cdsEnd);
    }

    public List<GenomicRegion> exons() {
        return exons;
    }

    public String getAccessionIdNoVersion() {
        return accessionIdNoVersion;
    }

    @Override
    public Strand strand() {
        return null;
    }

    @Override
    public Transcript withStrand(Strand other) {
        if (strand == other) {
            return this;
        } else {
            Position cdsStartOnPositive = cdsStart.invert(contig, coordinateSystem);
            Position cdsEndOnPositive = cdsEnd.invert(contig, coordinateSystem);
            List<GenomicRegion> exonsOnPositive = new ArrayList<>(exons.size());
            for (int i = exons.size() - 1; i >= 0; i--) {
                GenomicRegion exon = exons.get(i);
                exonsOnPositive.add(exon.withStrand(other));
            }
            return new Transcript(super.withStrand(other),
                    accessionId, hgvsSymbol, isCoding,
                    cdsEndOnPositive, cdsStartOnPositive, exonsOnPositive);
        }
    }

    public int getProteinLength() {
        if (! this.isCoding) {
            return 0;
        }
        Transcript t;
        if (strand() == Strand.NEGATIVE) {
            t = this.withStrand(Strand.POSITIVE);
        } else {
            t = this;
        }
        GenomicRegion cds = t.cdsRegion();
        int cdsStart = t.cdsStart().posOneBased();
        int cdsEnd   = t.cdsEnd().posOneBased();
        int cdsNtCount = 0;
        for (GenomicRegion exon : t.exons()) {
            int exonStart = exon.startGenomicPosition().posOneBased();
            int exonEnd = exon.endGenomicPosition().posOneBased();
            if (cds.contains(exon)) {
                cdsNtCount += exon.length();
            } else if (! cds.overlapsWith(exon)) {
                continue;
                // completely non-coding exon
                // past this point, either an exon is partially 5UTR or partially 3UTR
            } else if (exonStart < cdsStart) {
                // start coding located in this exon
                int exonLen = exonEnd - cdsStart;
                cdsNtCount += exonLen;
            } else if (exonEnd > cdsEnd) {
                int exonLen = cdsEnd - exonStart;
                cdsNtCount += exonLen;
            }
        }
        if (cdsNtCount % 3 != 0) {
            // should never happen
            throw new IsopretRuntimeException("Invalid amino acid length determined for " + t.accessionId() +": " + cdsNtCount);
        } else {
            return cdsNtCount / 3 - 1;
        }
    }


    @Override
    public Contig contig() {
        return this.txRegion.contig();
    }

    @Override
    public Position startPosition() {
        return this.txRegion.startPosition();
    }

    @Override
    public Position endPosition() {
        return this.txRegion.endPosition();
    }

    @Override
    public CoordinateSystem coordinateSystem() {
        return this.txRegion.coordinateSystem();
    }

    @Override
    public Transcript withCoordinateSystem(CoordinateSystem other) {
        if (this.txRegion.coordinateSystem() == other) {
            return this;
        } else {
            Position otherTxStart = txRegion.startPosition().shift(txRegion.coordinateSystem().startDelta(other));
            Position otherTxEnd = txRegion.endPosition().shift(txRegion.coordinateSystem().startDelta(other));
            Position otherCdsStart = cdsStart.shift(txRegion.coordinateSystem().startDelta(other));
            Position otherCdsEnd = cdsStart.shift(txRegion.coordinateSystem().startDelta(other));
            List<GenomicRegion> exonsWithCoordinateSystem = new ArrayList<>(exons.size());
            for (GenomicRegion region : exons) {
                GenomicRegion exon = region.withCoordinateSystem(other);
                exonsWithCoordinateSystem.add(exon);
            }
            GenomicRegion otherTxRegion = GenomicRegion.of(txRegion.contig(), txRegion.strand(), other, otherTxStart, otherTxEnd);
            return new Transcript(otherTxRegion,
                    accessionId, hgvsSymbol, isCoding,
                    otherCdsStart, otherCdsEnd, exonsWithCoordinateSystem);
        }
    }

    @Override
    public Transcript toOppositeStrand() {
        return withStrand(strand().opposite());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transcript that = (Transcript) o;
        return isCoding == that.isCoding &&
                Objects.equals(txRegion, that.txRegion) &&
                Objects.equals(accessionId, that.accessionId) &&
                Objects.equals(hgvsSymbol, that.hgvsSymbol) &&
                Objects.equals(cdsStart, that.cdsStart) &&
                Objects.equals(cdsEnd, that.cdsEnd) &&
                Objects.equals(exons, that.exons);
    }

    @Override
    public int hashCode() {
        return Objects.hash(txRegion, accessionId, hgvsSymbol, isCoding, cdsStart, cdsEnd, exons);
    }

    @Override
    public String toString() {
        return "Transcript{" +
                "accessionId='" + accessionId + '\'' +
                ", hgvsSymbol='" + hgvsSymbol + '\'' +
                ", isCoding=" + isCoding +
                ", cdsStart=" + cdsStart +
                ", cdsEnd=" + cdsEnd +
                ", exons=" + exons +
                "} " + super.toString();
    }
}
