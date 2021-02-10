package org.jax.isopret.transcript;

import org.jax.isopret.except.IsopretRuntimeException;
import org.monarchinitiative.svart.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class Transcript extends BaseGenomicRegion<Transcript> {

    private final String accessionId;
    /**
     * e.g., if {@link #accessionId} is ENST0000064021.6 then this would be ENST0000064021
     */
    private final String accessionIdNoVersion;

    private final String hgvsSymbol;

    private final boolean isCoding;

    private final List<GenomicRegion> exons;

    private final GenomicRegion cdsRegion;

    private Transcript(Contig contig,
                       Strand strand,
                       CoordinateSystem coordinateSystem,
                       Position start,
                       Position end,
                       String accessionId,
                       String hgvsSymbol,
                       boolean isCoding,
                       GenomicRegion cds,
                       List<GenomicRegion> exons) {
        super(contig, strand, coordinateSystem, start, end);
        if (isCoding) {
            this.cdsRegion = cds;
        } else {
            this.cdsRegion = null;
        }
        this.accessionId = accessionId;
        int i = this.accessionId.indexOf(".");
        if (i < 0) {
            this.accessionIdNoVersion = this.accessionId;
        } else {
            this.accessionIdNoVersion = this.accessionId.substring(0, i);
        }
        this.hgvsSymbol = hgvsSymbol;
        this.isCoding = isCoding;
        this.exons = exons;
    }

    public static Transcript of(Contig contig,
                                Strand strand,
                                CoordinateSystem coordinateSystem,
                                int start,
                                int end,
                                GenomicRegion cds,
                                String accessionId,
                                String hgvsSymbol,
                                boolean isCoding,
                                List<GenomicRegion> exons) {
        return new Transcript(contig,
                strand,
                coordinateSystem,
                Position.of(start),
                Position.of(end),
                accessionId,
                hgvsSymbol,
                isCoding,
                cds,
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

    public Optional<GenomicRegion> cdsRegion() {
        if (!isCoding()) {
            return Optional.empty();
        } else {
            return Optional.of(cdsRegion);
        }
    }

    public Optional<Position> cdsStart() {
        if (!isCoding()) {
            return Optional.empty();
        } else {
            return Optional.of(cdsRegion.startPosition());
        }
    }

    public Optional<Position> cdsEnd() {
        if (!isCoding()) {
            return Optional.empty();
        } else {
            return Optional.of(cdsRegion.endPosition());
        }
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
            Position spos = startPosition().invert(this.coordinateSystem(), contig());
            // Position cdsStartOnPositive = cdsStart.invert(contig, coordinateSystem);
            Position endpos = endPosition().invert(this.coordinateSystem(), contig());
            List<GenomicRegion> exonsWithOther = new ArrayList<>(exons.size());
            for (int i = exons.size() - 1; i >= 0; i--) {
                GenomicRegion exon = exons.get(i);
                exonsWithOther.add(exon.withStrand(other));
            }
            return new Transcript(contig(),
                    strand(),
                    coordinateSystem(),
                    endpos,
                    spos,
                    accessionId,
                    hgvsSymbol,
                    isCoding,
                    isCoding ? cdsRegion.withStrand(other) : null,
                    exonsWithOther);
        }
    }

    public int getProteinLength() {
        if (!this.isCoding) {
            return 0;
        }
        Transcript t;
        if (strand() == Strand.NEGATIVE) {
            t = this.withStrand(Strand.POSITIVE);
        } else {
            t = this;
        }
        int cdsStart = cdsRegion.startWithCoordinateSystem(CoordinateSystem.oneBased());
        int cdsEnd = cdsRegion.endWithCoordinateSystem(CoordinateSystem.oneBased());
        int cdsNtCount = 0;
        for (GenomicRegion exon : t.exons()) {
            int exonStart = exon.startWithCoordinateSystem(CoordinateSystem.oneBased());
            int exonEnd = exon.endWithCoordinateSystem(CoordinateSystem.oneBased());
            if (cdsRegion.contains(exon)) {
                cdsNtCount += exon.length();
            } else if (!cdsRegion.overlapsWith(exon)) {
                continue;
                // completely non-coding exon
                // past this point, either an exon is partially 5UTR or partially 3UTR
            } else if (exonStart < cdsStart) {
                // start coding located in this exon
                cdsNtCount += exon.overlapLength(cdsRegion);
            } else if (exonEnd > cdsEnd) {
                cdsNtCount += exon.overlapLength(cdsRegion);
            }
        }
        if (cdsNtCount % 3 != 0) {
            // should never happen
            throw new IsopretRuntimeException("Invalid amino acid length determined for " + t.accessionId() + ": " + cdsNtCount);
        } else {
            return cdsNtCount / 3 - 1;
        }
    }

    @Override
    public Transcript withCoordinateSystem(CoordinateSystem other) {
        if (coordinateSystem() == other) {
            return this;
        } else {
            List<GenomicRegion> exonsWithCoordinateSystem = new ArrayList<>(exons.size());
            for (GenomicRegion region : exons) {
                GenomicRegion exon = region.withCoordinateSystem(other);
                exonsWithCoordinateSystem.add(exon);
            }
            //  CDS is null if noncoding
            return new Transcript(contig(),
                    strand(),
                    coordinateSystem(),
                    Position.of(startWithCoordinateSystem(other)),
                    Position.of(endWithCoordinateSystem(other)),
                    accessionId,
                    hgvsSymbol,
                    isCoding,
                    isCoding ? cdsRegion.withCoordinateSystem(other) : null,
                    exonsWithCoordinateSystem);
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
        if (!super.equals(o)) return false;
        Transcript that = (Transcript) o;
        return isCoding == that.isCoding &&
                accessionId.equals(that.accessionId) &&
                accessionIdNoVersion.equals(that.accessionIdNoVersion) &&
                hgvsSymbol.equals(that.hgvsSymbol) &&
                exons.equals(that.exons) &&
                Objects.equals(cdsRegion, that.cdsRegion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), accessionId, accessionIdNoVersion, hgvsSymbol, isCoding, exons, cdsRegion);
    }

    @Override
    public String toString() {
        return "Transcript{" +
                "accessionId='" + accessionId + '\'' +
                ", hgvsSymbol='" + hgvsSymbol + '\'' +
                ", isCoding=" + isCoding +
                ", exons=" + exons +
                "} " + super.toString();
    }
}
