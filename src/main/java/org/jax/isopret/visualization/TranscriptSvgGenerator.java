package org.jax.isopret.visualization;


import org.jax.isopret.except.IsopretRuntimeException;
import org.jax.isopret.hbadeals.HbaDealsResult;
import org.jax.isopret.hbadeals.HbaDealsTranscriptResult;
import org.jax.isopret.transcript.AnnotatedGene;
import org.jax.isopret.transcript.Transcript;
import org.monarchinitiative.variant.api.*;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * Structural variant (SV) Scalar Vector Graphic (SVG) generator.
 *
 * @author Peter N Robinson
 */
public class TranscriptSvgGenerator extends AbstractSvgGenerator {
    static final int SVG_WIDTH = 1400;

    static final int HEIGHT_FOR_SV_DISPLAY = 200;
    static final int HEIGHT_PER_DISPLAY_ITEM = 80;

    /** Canvas height of the SVG.*/
    protected int SVG_HEIGHT;
    /** List of transcripts that are affected by the SV and that are to be shown in the SVG. */
    protected final List<Transcript> affectedTranscripts;
    /** List of log fold changes  -- key is the accession id from {@link #affectedTranscripts}. */
    private final Map<String, Double> foldChanges;

    /** Boundaries of SVG we do not write to. */
    private final double OFFSET_FACTOR = 0.1;

    private final double SVG_OFFSET = SVG_WIDTH * OFFSET_FACTOR;
    /** Number of base pairs from left to right boundary of the display area */
    private final double genomicSpan;
    /** Leftmost position (most 5' on chromosome). */
    protected final int genomicMinPos;
    /** Rightmost position (most 3' on chromosome). */
    protected final int genomicMaxPos;
    /** Equivalent to {@link #genomicMinPos} minus an offset so that the display items are not at the very edge. */
    protected final int paddedGenomicMinPos;
    /** Equivalent to {@link #genomicMaxPos} plus an offset so that the display items are not at the very edge. */
    protected final int paddedGenomicMaxPos;
    /** Number of base pairs from left to right boundary of the entire canvas */
    private final double paddedGenomicSpan;
    /** Minimum position of the scale TODO shouldnt this be {@link #genomicMinPos} ??? */
    private final double scaleMinPos;

    private final double scaleMaxPos;

    private final int scaleBasePairs;


    protected final double INTRON_MIDPOINT_ELEVATION = 10.0;
    /** Height of the symbols that represent the transcripts */
    private final double EXON_HEIGHT = 20;
    /** Y skip to put text underneath transcripts. Works with {@link #writeTranscriptName}*/
    protected final double Y_SKIP_BENEATH_TRANSCRIPTS = 50;
    /** Height of the symbol that represents the structural variant. */
    protected final double SV_HEIGHT = 30;


    private final HbaDealsResult hbaDealsResult;

    /**
     * TODO -- make sure we deal with version numbers in a uniform way. Now, we
     * are assuming that the HBADeals object does NOT use a version number.
     * @param annotatedGene an object with all information about a gene that is relevant for making the SVG/HTML output
     * @return list of transcripts
     */
    private List<Transcript> getAffectedTranscripts(AnnotatedGene annotatedGene) {
        List<Transcript> transcripts = annotatedGene.getTranscripts();
        HbaDealsResult result = annotatedGene.getHbaDealsResult();
        Map<String, HbaDealsTranscriptResult> transcriptMap = result.getTranscriptMap();
        List<Transcript> affectedTranscripts = transcripts
                .stream()
                .filter(t -> transcriptMap.containsKey(t.getAccessionIdNoVersion()))
                .collect(Collectors.toList());
        return affectedTranscripts;
    }


    /**
     * The constructor calculates the left and right boundaries for display
     * TODO document logic, cleanup
     *  @param atranscript Object with transcripts and annotations
     */
    private TranscriptSvgGenerator(int height, AnnotatedGene atranscript) {
        super(SVG_WIDTH,height);
        this.affectedTranscripts = getAffectedTranscripts(atranscript);
        this.hbaDealsResult = atranscript.getHbaDealsResult();
        this.foldChanges = getFoldChangesOfAffectedTranscripts(atranscript);

        this.genomicMinPos= affectedTranscripts.stream()
                .map(t -> t.withStrand(Strand.POSITIVE))
                .mapToInt(GenomicRegion::start)
                .min()
                .orElse(0);
        this.genomicMaxPos = affectedTranscripts.stream()
                .map(t -> t.withStrand(Strand.POSITIVE))
                .mapToInt(GenomicRegion::end)
                .max()
                .orElse(this.genomicMinPos + 1000); // We should never actually need the orElse
        this.genomicSpan = this.genomicMaxPos - this.genomicMinPos;
        this.paddedGenomicMinPos = genomicMinPos - (int)(0.05*(this.genomicSpan));
        this.paddedGenomicMaxPos = genomicMaxPos + (int)(0.23*(this.genomicSpan));
        this.paddedGenomicSpan = this.paddedGenomicMaxPos - this.paddedGenomicMinPos;
        this.scaleBasePairs = 1 + this.genomicMaxPos  -  this.genomicMinPos;
        this.scaleMinPos = translateGenomicToSvg(genomicMinPos);
        this.scaleMaxPos = translateGenomicToSvg(genomicMaxPos);

    }




    /**
     * Transform a genomic coordinate to an SVG X coordinate
     *
     * @return
     */
    protected double translateGenomicToSvg(int genomicCoordinate) {
        double pos = genomicCoordinate - paddedGenomicMinPos;
        if (pos < 0) {
            throw new IsopretRuntimeException("Bad left boundary (genomic coordinate-"); // should never happen
        }
        double prop = pos / paddedGenomicSpan;
        return prop * SVG_WIDTH;
    }


    /**
     * Write a coding exon
     *
     * @param start
     * @param end
     * @param ypos
     * @param writer
     * @throws IOException
     */
    protected void writeCdsExon(double start, double end, int ypos, Writer writer) throws IOException {
        double width = end - start;
        double Y = ypos - 0.5 * EXON_HEIGHT;
        String rect = String.format("<rect x=\"%f\" y=\"%f\" width=\"%f\" height=\"%f\" rx=\"2\" " +
                        "style=\"stroke:%s; fill: %s\" />\n",
                start, Y, width, EXON_HEIGHT, DARKGREEN, GREEN);
        writer.write(rect);
    }

    /**
     * WRite a non-coding (i.e., UTR) exon of a non-coding gene
     *
     * @param start start position in SVG coordinates of the UTR exon
     * @param end end position in SVG coordinates of the UTR exon
     * @param ypos yposition on SVG canvas
     * @param writer file handle
     * @throws IOException if we cannot write
     */
    protected void writeUtrExon(double start, double end, int ypos, Writer writer) throws IOException {
        double width = end - start;
        double Y = ypos - 0.5 * EXON_HEIGHT;
        String rect = String.format("<rect x=\"%f\" y=\"%f\" width=\"%f\" height=\"%f\"  " +
                        "style=\"stroke:%s; fill: %s\" />\n",
                start, Y, width, EXON_HEIGHT, DARKGREEN, YELLOW);
        writer.write(rect);
    }


    /**
     * This method writes one Jannovar transcript as a cartoon where the UTRs are shown in one color and the
     * the coding exons are shown in another color. TODO -- decide what to do with non-coding genes
     *
     * @param tmod   transcript representation
     * @param ypos   The y position where we will write the cartoon
     * @param writer file handle
     * @throws IOException if we cannot write.
     */
    private void writeCodingTranscript(Transcript tmod, int ypos, Writer writer) throws IOException {
        Transcript transcript = tmod.withStrand(Strand.POSITIVE);
        GenomicRegion cds = transcript.cdsRegion();

        double cdsStart = translateGenomicToSvg(cds.start());
        double cdsEnd = translateGenomicToSvg(cds.end());
        List<GenomicRegion> exons = transcript.exons();
        double minX = Double.MAX_VALUE;
        // write a line for UTR, otherwise write a box
        for (GenomicRegion exon : exons) {
            double exonStart = translateGenomicToSvg(exon.start());
            double exonEnd = translateGenomicToSvg(exon.end());
            if (exonStart < minX) minX = exonStart;
            if (exonStart >= cdsStart && exonEnd <= cdsEnd) {
                writeCdsExon(exonStart, exonEnd, ypos, writer);
            } else if (exonStart <= cdsEnd && exonEnd > cdsEnd) {
                // in this case, the 3' portion of the exon is UTR and the 5' is CDS
                writeCdsExon(exonStart, cdsEnd, ypos, writer);
                writeUtrExon(cdsEnd, exonEnd, ypos, writer);
            } else if (exonStart < cdsStart && exonEnd > cdsStart) {
                writeUtrExon(exonStart, cdsStart, ypos, writer);
                writeCdsExon(cdsStart, exonEnd, ypos, writer);
            } else {
                writeUtrExon(exonStart, exonEnd, ypos, writer);
            }
        }
        writeIntrons(exons, ypos, writer);
        writeTranscriptName(tmod, minX, ypos, writer);
       writeFoldChange(transcript.getAccessionIdNoVersion(), ypos, writer);
    }

    private void writeNonCodingTranscript(Transcript tmod, int ypos, Writer writer) throws IOException {
        Transcript transcript = tmod.withStrand(Strand.POSITIVE);
        List<GenomicRegion> exons = transcript.exons();
        double minX = Double.MAX_VALUE;
        // write a line for UTR, otherwise write a box
        for (GenomicRegion exon : exons) {
            double exonStart = translateGenomicToSvg(exon.start());
            double exonEnd = translateGenomicToSvg(exon.end());
            if (exonStart < minX) minX = exonStart;
            writeUtrExon(exonStart, exonEnd, ypos, writer);
        }
        writeIntrons(exons, ypos, writer);
        writeTranscriptName(transcript, minX, ypos, writer);
        writeFoldChange(transcript.getAccessionIdNoVersion(), ypos, writer);
    }


        private Map<String, Double> getFoldChangesOfAffectedTranscripts(AnnotatedGene atranscript) {
        Map<String, Double> foldchanges = new HashMap<>();
        HbaDealsResult result = atranscript.getHbaDealsResult();
        Map<String, HbaDealsTranscriptResult> transcriptResultMap = result.getTranscriptMap();
        for (Transcript transcript : this.affectedTranscripts) {
            String id = transcript.getAccessionIdNoVersion();
            if (transcriptResultMap.containsKey(id)) {
                double fc = transcriptResultMap.get(id).getFoldChange();
                double logFC = Math.log(fc)/ Math.log(2);
                foldchanges.put(id , logFC);
            } else {
                foldchanges.put(id, 0.0);
            }
        }
        return foldchanges;
    }

    private double getLogFoldChage(String id) {
        Map<String, HbaDealsTranscriptResult> transcriptResultMap = hbaDealsResult.getTranscriptMap();
        if (! transcriptResultMap.containsKey(id)) return 0.0;
        double fc = transcriptResultMap.get(id).getFoldChange();
        return Math.log(fc) / Math.log(2);
    }

    private String getFormatedPvalue(String id) {
        Map<String, HbaDealsTranscriptResult> transcriptResultMap = hbaDealsResult.getTranscriptMap();
        double logFC = getLogFoldChage(id);
        if (! transcriptResultMap.containsKey(id)) return String.valueOf(logFC);
        double p =  transcriptResultMap.get(id).getP();
        if (p >= 0.05) {
            return String.format("%.2f (n.s.)", logFC);
        } else if (p > 0.001) {
            return String.format("%.2f (p=%.4f)", logFC, p);
        }
        return String.format("%.2f (p=%.2E)", logFC, p);
    }


    private void writeFoldChange(String id, int ypos, Writer writer) throws IOException {
        double fc = getLogFoldChage(id);
        double startpos = translateGenomicToSvg(this.genomicMaxPos) + 25.0;
        double y = ypos;
        writer.write(String.format("<line x1=\"%f\" y1=\"%f\" x2=\"%f\" y2=\"%f\" stroke=\"black\"/>\n",
                startpos, y, startpos+30, y));
        double width = 20.0;
        double boxstart = startpos + 5.0;
        double factor = 25; // multiple logFC by this to get height
        String rect;
        if (fc > 0.0) {
            double height = fc*factor;
            double ybase = y - height;
            rect = String.format("<rect x=\"%f\" y=\"%f\" width=\"%f\" height=\"%f\" rx=\"2\" " +
                            "style=\"stroke:%s; fill: %s\" />\n",
                    boxstart, ybase, width, height, BLACK, GREEN);
        } else {
            double height = fc*-factor;
            rect = String.format("<rect x=\"%f\" y=\"%f\" width=\"%f\" height=\"%f\" rx=\"2\" " +
                            "style=\"stroke:%s; fill: %s\" />\n",
                    boxstart, y, width, height, BLACK, RED);
        }
        writer.write(rect);
        double xpos = startpos + width + 15;
        String txt = String.format("<text x=\"%f\" y=\"%f\" fill=\"%s\">%s</text>\n",
                xpos, y, PURPLE, getFormatedPvalue(id));
        writer.write(txt);

    }


    /**
     * Write a line to indicate transcript (UTR) or a dotted line to indicate introns. The line forms
     * a triangle (inspired by the way Ensembl represents introns).
     *
     * @param exons list of exons on {@link Strand#POSITIVE} in sorted order (chromosome 5' to 3')
     * @param ypos  vertical midline
     * @throws IOException if we cannot write
     */
    private void writeIntrons(List<GenomicRegion> exons, int ypos, Writer writer) throws IOException {
        // if the gene does not have an intron, we are done
        if (exons.size() == 1)
            return;
        List<Integer> intronStarts = new ArrayList<>();
        List<Integer> intronEnds = new ArrayList<>();
        for (int i = 1; i < exons.size(); i++) {
            GenomicRegion previous = exons.get(i - 1);
            GenomicRegion current = exons.get(i);
            intronStarts.add(previous.end());
            intronEnds.add(current.start());
        }
        for (int i = 0; i < intronStarts.size(); i++) {
            double startpos = translateGenomicToSvg(intronStarts.get(i));
            double endpos = translateGenomicToSvg(intronEnds.get(i));
            double midpoint = 0.5 * (startpos + endpos);
            double Y = ypos;
            writer.write(String.format("<line x1=\"%f\" y1=\"%f\" x2=\"%f\" y2=\"%f\" stroke=\"black\"/>\n",
                    startpos, Y, midpoint, Y - INTRON_MIDPOINT_ELEVATION));
            writer.write(String.format("<line x1=\"%f\" y1=\"%f\" x2=\"%f\" y2=\"%f\" stroke=\"black\"/>\n",
                    midpoint, Y - INTRON_MIDPOINT_ELEVATION, endpos, Y));
        }
    }

    private void writeTranscriptName(Transcript tmod, double xpos, int ypos, Writer writer) throws IOException {
        String symbol = tmod.hgvsSymbol();
        String accession = tmod.accessionId();
        String chrom = tmod.contigName();
        Transcript txOnFwdStrand = tmod.withStrand(Strand.POSITIVE);
        int start = txOnFwdStrand.start();
        int end = txOnFwdStrand.end();
        String strand = tmod.strand().toString();
        String positionString = String.format("%s:%d-%d (%s strand)", chrom, start, end, strand);
        String geneName = String.format("%s (%s)", symbol, accession);
        double y = Y_SKIP_BENEATH_TRANSCRIPTS + ypos;
        String txt = String.format("<text x=\"%f\" y=\"%f\" fill=\"%s\">%s</text>\n",
                xpos, y, PURPLE, String.format("%s  %s", geneName, positionString));
        writer.write(txt);
    }


    protected void writeScale(Writer writer, int ypos) throws IOException {
        int verticalOffset = 10;
        String line = String.format("<line x1=\"%f\" y1=\"%d\"  x2=\"%f\"  y2=\"%d\" style=\"stroke: #000000; fill:none;" +
                " stroke-width: 1px;" +
                " stroke-dasharray: 5 2\" />\n", this.scaleMinPos, ypos, this.scaleMaxPos, ypos);
        String leftVertical = String.format("<line x1=\"%f\" y1=\"%d\"  x2=\"%f\"  y2=\"%d\" style=\"stroke: #000000; fill:none;" +
                " stroke-width: 1px;\" />\n", this.scaleMinPos, ypos + verticalOffset, this.scaleMinPos, ypos - verticalOffset);
        String rightVertical = String.format("<line x1=\"%f\" y1=\"%d\"  x2=\"%f\"  y2=\"%d\" style=\"stroke: #000000; fill:none;" +
                " stroke-width: 1px;\" />\n", this.scaleMaxPos, ypos + verticalOffset, this.scaleMaxPos, ypos - verticalOffset);
        String sequenceLength = getSequenceLengthString(scaleBasePairs);
        writer.write(line);
        writer.write(leftVertical);
        writer.write(rightVertical);
        int y = ypos - 15;
        double xmiddle = 0.45 * (this.scaleMinPos + this.scaleMaxPos);
        String txt = String.format("<text x=\"%f\" y=\"%d\" fill=\"%s\">%s</text>\n",
                xmiddle, y, PURPLE, sequenceLength);
        writer.write(txt);
    }

    protected void writeScale(Writer writer, Contig contig, int ypos) throws IOException {
        int verticalOffset = 10;
        String line = String.format("<line x1=\"%f\" y1=\"%d\"  x2=\"%f\"  y2=\"%d\" style=\"stroke: #000000; fill:none;" +
                " stroke-width: 1px;" +
                " stroke-dasharray: 5 2\" />\n", this.scaleMinPos, ypos, this.scaleMaxPos, ypos);
        String leftVertical = String.format("<line x1=\"%f\" y1=\"%d\"  x2=\"%f\"  y2=\"%d\" style=\"stroke: #000000; fill:none;" +
                " stroke-width: 1px;\" />\n", this.scaleMinPos, ypos + verticalOffset, this.scaleMinPos, ypos - verticalOffset);
        String rightVertical = String.format("<line x1=\"%f\" y1=\"%d\"  x2=\"%f\"  y2=\"%d\" style=\"stroke: #000000; fill:none;" +
                " stroke-width: 1px;\" />\n", this.scaleMaxPos, ypos + verticalOffset, this.scaleMaxPos, ypos - verticalOffset);
        String sequenceLength = getSequenceLengthString(scaleBasePairs);
        writer.write(line);
        writer.write(leftVertical);
        writer.write(rightVertical);
        int y = ypos - 15;
        double xmiddle = 0.45 * (this.scaleMinPos + this.scaleMaxPos);
        double xcloseToStart = 0.1 * (this.scaleMinPos + this.scaleMaxPos);
        String txt = String.format("<text x=\"%f\" y=\"%d\" fill=\"%s\">%s</text>\n",
                xmiddle, y, PURPLE, sequenceLength);
        writer.write(txt);
        String contigName = contig.ucscName();
        txt = String.format("<text x=\"%f\" y=\"%d\" fill=\"%s\">%s</text>\n",
                xcloseToStart, y, PURPLE, contigName);
        writer.write(txt);
    }


    /**
     * Get a string that represents a sequence length using bp, kb, or Mb as appropriate
     *
     * @param seqlen number of base bairs
     * @return String such as 432 bp, 4.56 kb or 1.23 Mb
     */
    protected String getSequenceLengthString(int seqlen) {
        if (seqlen < 1_000) {
            return String.format("%d bp", seqlen);
        } else if (seqlen < 1_000_000) {
            double kb = (double) seqlen / 1000.0;
            return String.format("%.2f kp", kb);
        } else {
            // if we get here, the sequence is at least one million bp
            double mb = (double) seqlen / 1000000.0;
            return String.format("%.2f Mp", mb);
        }
    }

    /**
     * Get a string containing an SVG representing the SV.
     *
     * @return an SVG string
     */
    public String getSvg() {
        StringWriter swriter = new StringWriter();
        try {
            writeHeader(swriter);
            write(swriter);
            writeFooter(swriter);
            return swriter.toString();
        } catch (IOException e) {
            return getSvgErrorMessage(e.getMessage());
        }
    }

    /**
     * PROTOTYPE -- THIS MAYBE NOT BE THE BEST WAY TO REPRESENT OTHER TUPES OF SV
     * @param ypos  The y position where we will write the cartoon
     * @param msg A String describing the SV
     * @param writer a file handle
     * @throws IOException if we can't write
     * TODO -- ADAPT THIS TO SHOW A BOX OVER AREAS THAT SHOW ALTERNATIVE SPLICING
     */
    private void writeDeletion(int ypos, String msg, Writer writer) throws IOException {
        double start = 42;//translateGenomicToSvg(variant.start());
        double end = 43;//translateGenomicToSvg(variant.end()); -- TODO
        double width = end - start;
        double Y = ypos + 0.5 * SV_HEIGHT;
        String rect = String.format("<rect x=\"%f\" y=\"%f\" width=\"%f\" height=\"%f\" rx=\"2\" " +
                        "style=\"stroke:%s; fill: %s\" />\n",
                start, Y, width, SV_HEIGHT, DARKGREEN, RED);
        writer.write(rect);
        Y += 1.75*SV_HEIGHT;
        writer.write(String.format("<text x=\"%f\" y=\"%f\"  fill=\"%s\">%s</text>\n",start -10,Y, PURPLE, msg));
    }

    /**
     * Wirte an SVG (without header) representing this SV. Not intended to be used to create a stand-alone
     * SVG (for this, user {@link #getSvg()}
     *
     * @param writer a file handle
     */
    @Override
    public void write(Writer writer)  {
        int starty = 50;
        int y = starty;
        // TODO WRITE SYMBOLS TO DESCRIBE THE ALTERNATE SPLICING
        y += 100;
        try {
            for (var tmod : this.affectedTranscripts) {
                if (tmod.isCoding()) {
                    writeCodingTranscript(tmod, y, writer);
                } else {
                    writeNonCodingTranscript(tmod, y, writer);
                }
                y += HEIGHT_PER_DISPLAY_ITEM;
            }
            writeScale(writer, y);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static AbstractSvgGenerator factory(AnnotatedGene annotatedTranscript) {
        List<Transcript> transcripts = annotatedTranscript.getTranscripts();
        HbaDealsResult result = annotatedTranscript.getHbaDealsResult();
        Map<String, HbaDealsTranscriptResult> transcriptMap = result.getTranscriptMap();
        List<Transcript> affectedTranscripts = transcripts
                .stream()
                .filter(t -> transcriptMap.containsKey(t.getAccessionIdNoVersion()))
                .collect(Collectors.toList());
        int height = HEIGHT_FOR_SV_DISPLAY + affectedTranscripts.size() * HEIGHT_PER_DISPLAY_ITEM;
        return new TranscriptSvgGenerator(height,
                annotatedTranscript);
    }

}
