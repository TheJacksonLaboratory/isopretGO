package org.jax.isopret.core.transcript;

import org.monarchinitiative.svart.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class Transcript extends BaseGenomicRegion<Transcript> {
    private static final Logger LOGGER = LoggerFactory.getLogger(Transcript.class);

    private final AccessionNumber accessionId;

    private final String hgvsSymbol;

    private final boolean isCoding;

    private final List<GenomicRegion> exons;

    private final GenomicRegion cdsRegion;

    private Transcript(Contig contig,
                       Strand strand,
                       CoordinateSystem coordinateSystem,
                       int start,
                       int end,
                       AccessionNumber accessionId,
                       String hgvsSymbol,
                       boolean isCoding,
                       GenomicRegion cds,
                       List<GenomicRegion> exons) {
        super(contig, strand, Coordinates.of(coordinateSystem, start, end));
        if (isCoding) {
            this.cdsRegion = cds;
        } else {
            this.cdsRegion = null;
        }
        this.accessionId = accessionId;
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
                                AccessionNumber accessionId,
                                String hgvsSymbol,
                                boolean isCoding,
                                List<GenomicRegion> exons) {
        return new Transcript(contig,
                strand,
                coordinateSystem,
                start,
                end,
                accessionId,
                hgvsSymbol,
                isCoding,
                cds,
                exons);
    }

    public AccessionNumber accessionId() {
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
            return Optional.ofNullable(cdsRegion);
        }
    }

    public Optional<Integer> cdsStart() {
        if (cdsRegion == null || !isCoding()) {
            return Optional.empty();
        } else {
            return Optional.of(cdsRegion.start());
        }
    }

    public Optional<Integer> cdsEnd() {
        if (cdsRegion == null || !isCoding()) {
            return Optional.empty();
        } else {
            return Optional.of(cdsRegion.end());
        }
    }

    public List<GenomicRegion> exons() {
        return exons;
    }



    /**
     * Returns an array whose entries correspond to the lengths of exons that are part of the CDS.
     * All entries are zero for non-coding genes.
     * @return an array of the length of the CDS portions of exons (individual entries can be zero).
     */
    public List<Integer> codingExonLengths() {
        List<Integer> cdsExonLengths = new ArrayList<>();
        for (GenomicRegion exon : exons) {
            cdsExonLengths.add(exon.overlapLength(this.cdsRegion));
        }
        return cdsExonLengths;
    }


    @Override
    public Transcript withStrand(Strand other) {
        // not needed
        throw new UnsupportedOperationException("withStrand operation not supported");
    }

    public int getMrnaLength() {
        return this.exons
                .stream()
                .mapToInt(GenomicRegion::length)
                .sum();
    }

    public int getProteinLength() {
        if (!this.isCoding) {
            return 0;
        }
        int cdsNtCount = 0;
        int i=0;
        for (GenomicRegion exon : exons()) {
            cdsNtCount += exon.overlapLength(cdsRegion);
        }
        // TODO -- Figure out what is going on here
        // lots of transcripts do not have 3n nt according to our calcs.
        if (cdsNtCount % 3 != 0) {
//            // should never happen
//            // a small number of Ensembl entries seem to be 3n+1 or 3n+2
//            // this should not matter fo visualization but log the error
            LOGGER.error("Invalid CDS length determined for " + accessionId() + ": " + cdsNtCount + " (bp not a multiple of 3)");
        }
        return cdsNtCount / 3 - 1; // remove one aa so we do not count the stop codon
    }

    @Override
    public Transcript withCoordinateSystem(CoordinateSystem other) {
        // not needed
        throw new UnsupportedOperationException("withStrand operation not supported");
    }


    @Override
    protected Transcript newRegionInstance(Contig contig, Strand strand, Coordinates  coordinates) {
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
                hgvsSymbol.equals(that.hgvsSymbol) &&
                exons.equals(that.exons) &&
                Objects.equals(cdsRegion, that.cdsRegion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), accessionId, hgvsSymbol, isCoding, exons, cdsRegion);
    }

    @Override
    public String toString() {
        return "Transcript{" +
                "accessionId='" + accessionId.getAccessionString() + '\'' +
                ", hgvsSymbol='" + hgvsSymbol + '\'' +
                ", isCoding=" + isCoding +
                ", exons=" + exons +
                "} " + super.toString();
    }
}
