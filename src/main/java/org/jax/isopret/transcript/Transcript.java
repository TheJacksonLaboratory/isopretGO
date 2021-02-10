package org.jax.isopret.transcript;

import org.jax.isopret.except.IsopretRuntimeException;
import org.monarchinitiative.svart.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Transcript extends BaseGenomicRegion<Transcript> {

    private final String accessionId;
    /** e.g., if {@link #accessionId} is ENST0000064021.6 then this would be ENST0000064021 */
    private final String accessionIdNoVersion;

    private final String hgvsSymbol;

    private final boolean isCoding;
    private final Position cdsStart;
    private final Position cdsEnd;
    private final List<GenomicRegion> exons;


    private Transcript(Contig contig,
                       Strand strand,
                       CoordinateSystem coordinateSystem,
                       Position start,
                       Position end,
                       String accessionId,
                       String hgvsSymbol,
                       boolean isCoding,
                       Position cdsStart,
                       Position cdsEnd,
                       List<GenomicRegion> exons) {
        super(contig, strand,coordinateSystem, start, end);
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
                                Strand strand,
                                CoordinateSystem coordinateSystem,
                                int start,
                                int end,
                                int cdsStart,
                                int cdsEnd,
                                String accessionId,
                                String hgvsSymbol,
                                boolean isCoding,
                                List<GenomicRegion> exons) {
        //GenomicRegion txRegion = PreciseGenomicRegion.of(contig, strand, coordinateSystem, Position.of(start), Position.of(end));
        return new Transcript(contig,
                strand,
                coordinateSystem,
                Position.of(start),
                Position.of(end),
                accessionId,
                hgvsSymbol,
                isCoding,
                Position.of(cdsStart),
                Position.of(cdsEnd),
                exons);
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
    // TODO Optional ?????
    public GenomicRegion cdsRegion() {
        return GenomicRegion.of(contig, strand, coordinateSystem(),cdsStart, cdsEnd);
    }
    // TODO Optional ?????
    public Position cdsStart() {
        return GenomicPosition.zeroBased(contig, strand, cdsStart);
    }
    // TODO Optional ?????
    public Optional<Position> cdsEnd() {
        return GenomicPosition.zeroBased(contig, strand, cdsEnd);
    }

    public List<GenomicRegion> exons() {
        return exons;
    }

    public String getAccessionIdNoVersion() {
        return accessionIdNoVersion;
    }

    @Override
    public Transcript withStrand(Strand other) {

        if (this.strand() == other) {
            return this;
        } else {
            Position spos= startPosition().invert(this.coordinateSystem(), contig());
           // Position cdsStartOnPositive = cdsStart.invert(contig, coordinateSystem);
            Position endpos= endPosition().invert(this.coordinateSystem(), contig());
            List<GenomicRegion> exonsOnPositive = new ArrayList<>(exons.size());
            for (int i = exons.size() - 1; i >= 0; i--) {
                GenomicRegion exon = exons.get(i);
                exonsOnPositive.add(exon.withStrand(other));
            }
            if (! this.isCoding) {
// dont care about CDS
            }
            // switch CDS also
            return new Transcript(super.withStrand(other),
                    accessionId, hgvsSymbol, isCoding,
                    endpos, startPosition(), exonsOnPositive);
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
        int cdsStart = cds.startWithCoordinateSystem(CoordinateSystem.oneBased());
        int cdsEnd   = cds.endWithCoordinateSystem(CoordinateSystem.oneBased());
        int cdsNtCount = 0;
        for (GenomicRegion exon : t.exons()) {
            int exonStart = exon.startWithCoordinateSystem(CoordinateSystem.oneBased());
            int exonEnd = exon.endWithCoordinateSystem(CoordinateSystem.oneBased());
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
    public Transcript withCoordinateSystem(CoordinateSystem other) {
        if (coordinateSystem() == other) {
            return this;
        } else {
            Position otherTxStart = startPositionWithCoordinateSystem(other);
            Position otherTxEnd = endPositionWithCoordinateSystem(other);
            // TODO MAKE CDS BE a genomic Region
            Position otherCdsStart = cdsStart.shift(txRegion.coordinateSystem().startDelta(other));
            Position otherCdsEnd = cdsStart.shift(txRegion.coordinateSystem().endDelta(other));
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
    protected Transcript newRegionInstance(Contig contig, Strand strand, CoordinateSystem coordinateSystem, Position startPosition, Position endPosition) {
        return null;
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
