package org.jax.isopret.core.impl.jannovar;

import de.charite.compbio.jannovar.reference.GenomeInterval;
import de.charite.compbio.jannovar.reference.TranscriptModel;

import org.jax.isopret.data.AccessionNumber;
import org.jax.isopret.data.Transcript;
import org.monarchinitiative.svart.*;

import org.monarchinitiative.svart.assembly.GenomicAssembly;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Class for remapping Jannovar {@link TranscriptModel} to our domain model.
 */
record JannovarTxMapper(GenomicAssembly assembly) {
    private static final Logger LOGGER = LoggerFactory.getLogger(JannovarTxMapper.class);

    Optional<Transcript> remap(TranscriptModel tm) {
        String contigName = tm.getTXRegion().getRefDict().getContigIDToName().get(tm.getChr());
        Contig contig = assembly.contigByName(contigName);
        if (contig == null) {
            LOGGER.warn("Unknown contig: `{}` in transcript `{}`", contigName, tm.getAccession());
            return Optional.empty();
        }

        // region spanned by exons & introns, including UTRs
        GenomeInterval txRegion = tm.getTXRegion();
        Strand strand = txRegion.getStrand().isForward()
                ? Strand.POSITIVE
                : Strand.NEGATIVE;

        // these coordinates are already adjusted to the appropriate strand
        GenomeInterval cdsRegion = tm.getCDSRegion();
        GenomicRegion cds = GenomicRegion.of(contig, strand, CoordinateSystem.zeroBased(),cdsRegion.getBeginPos(), cdsRegion.getEndPos());
        // cds is ignored if not coding.
        // process exons
        List<GenomicRegion> exons = new ArrayList<>();
        for (GenomeInterval exon : tm.getExonRegions()) {
            exons.add(GenomicRegion.of(contig, strand, CoordinateSystem.zeroBased(), exon.getBeginPos(), exon.getEndPos()));
        }

        AccessionNumber transcriptAccession = AccessionNumber.ensemblTranscript(tm.getAccession());

        return Optional.of(Transcript.of(contig, strand, CoordinateSystem.zeroBased(), txRegion.getBeginPos(), txRegion.getEndPos(),
                cds, transcriptAccession, tm.getGeneSymbol(), tm.isCoding(),
                exons));
    }
}