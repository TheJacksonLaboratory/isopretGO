package org.jax.isopret.visualization;


import org.jax.isopret.interpro.DisplayInterproAnnotation;
import org.jax.isopret.interpro.InterproEntry;
import org.jax.isopret.transcript.AccessionNumber;
import org.jax.isopret.transcript.AnnotatedGene;
import org.jax.isopret.transcript.Transcript;


import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Write an SVG representing the protein domains and corresponding exon structure.
 * @author Peter N Robinson
 */
public class ProteinSvgGenerator extends AbstractSvgGenerator {
    private static final int SVG_WIDTH = 1050;
    private static final int HEIGHT_FOR_SV_DISPLAY = 160;
    private static final int HEIGHT_PER_DISPLAY_ITEM = 90;
    private static final int ISOFORM_HEIGHT = 20;
    private static final String[] colors = {"#F08080", "#CCE5FF", "#ABEBC6", "#FFA07A", "#C39BD3", "#FEA6FF", "#F7DC6F",
            "#CFFF98", "#A1D6E2", "#EC96A4", "#E6DF44", "#F76FDA", "#FFCCE5", "#E4EA8C", "#F1F76F", "vFDD2D6", "#F76F7F",
            "#DAF7A6", "#FFC300", "#F76FF5", "#FFFF99", "#FF99FF", "#99FFFF", "#CCFF99", "#FFE5CC", "#FFD700", "#9ACD32",
            "#7FFFD4", "#FFB6C1", "#FFFACD", "#FFE4E1", "#F0FFF0", "#F0FFFF"};

    private final SortedMap<InterproEntry, String> interproEntryColorMap;

    private final AnnotatedGene annotatedGene;

    private final int maxProteinLength;

    private final int proteinMinSvgPos;
    private final int proteinMaxSvgPos;
    private final int keyMinSvgPos;
    /** Color used for exon boxes. */
    private final static String JAMA_BLUE = "#00b2e2";

    private final static int HEIGHT_PER_INTERPRO_ROW = 30;

    private ProteinSvgGenerator(int height, AnnotatedGene annotatedGene, SortedMap<InterproEntry, String> colorMap) {
        super(SVG_WIDTH, height);
        this.annotatedGene = annotatedGene;
        this.interproEntryColorMap = colorMap;
        this.maxProteinLength = annotatedGene
                .getExpressedTranscripts()
                .stream()
                .map(Transcript::getProteinLength)
                .max(Integer::compareTo)
                .orElse(1000);
        this.proteinMinSvgPos = 50;
        this.proteinMaxSvgPos = 1000;
        this.keyMinSvgPos = 1050;
    }


    /**
     * Transform an amino-acid coordinate to an SVG X coordinate
     *
     * @return coordinate of a given protein amino acid position in SVG space
     */
    protected double translateProteinToSvgCoordinate(int aminoAcidPosition) {
        final double span = this.proteinMaxSvgPos - this.proteinMinSvgPos;
        double prop = (double) aminoAcidPosition / (double) maxProteinLength;
        return this.proteinMinSvgPos + prop * span;
    }


    /**
     * Write a box for the entire transcript with accession number right on top of the box.
     * @param ypos SVG Y coordinate to write the protein box
     * @param xend end position of this protein in SVG coordinates.
     * @param writer file handle
     * @throws IOException if we cannot write the box
     */
    protected void writeProteinBox(Transcript transcript, double xend, int ypos, double logFC, Writer writer) throws IOException {
        double xstart = this.proteinMinSvgPos;
        double width = xend - xstart;
        double Y = ypos - 0.5 * ISOFORM_HEIGHT;
        String rect = String.format("<rect x=\"%f\" y=\"%f\" width=\"%f\" height=\"%d\" " +
                        "style=\"stroke:%s; fill:none \" />\n",
                xstart, Y, width, ISOFORM_HEIGHT, BLACK);
        writer.write(rect);
        Y = ypos - 0.55 * ISOFORM_HEIGHT;
        String payload = String.format("%s (log-fold change: %.2f)",transcript.accessionId().getAccessionString(), logFC);
        String label = SvgUtil.text(xstart+5, Y-5, PURPLE, 24, payload);
        writer.write(label);
    }

    private void writeExons(Transcript transcript, double xend, int ypos, Writer writer) throws IOException{
        double xstart = this.proteinMinSvgPos;
        double width = xend - xstart;
        List<Integer> codingExonLengths = transcript.codingExonLengths();
        int cdnalen = codingExonLengths.stream().mapToInt(Integer::intValue).sum();
        double widthPerBp = width/cdnalen;
        int exonNumber = 0;
        double x1 = xstart;
        for (int cdsLen : codingExonLengths) {
            exonNumber++;
            if (cdsLen == 0) continue;
            double exonwidth =  cdsLen * widthPerBp;
            writeExon(x1, exonwidth, exonNumber, ypos, writer);
            x1 += exonwidth;
        }
    }

    private void writeExon(double x1, double exonwidth , int exonNum, int ypos, Writer writer) throws IOException {
        String rect = String.format("<rect x=\"%f\" y=\"%d\" width=\"%f\" height=\"%d\" " +
                        "style=\"fill:%s;stroke:black;stroke-width:1\" />\n",
                x1, ypos, exonwidth, ISOFORM_HEIGHT, JAMA_BLUE);
        writer.write(rect);
        final double digitWidth = 4;
        if (exonwidth > 20) {
            double text_x = x1 + exonwidth/2;
            text_x -= exonNum>9 ? 2*digitWidth : digitWidth; // adjust position for two-digit exon numbers
            double text_y = ypos + 0.75*ISOFORM_HEIGHT;
            writer.write(String.format("<text x=\"%f\" y=\"%f\" fill=\"white\">%d</text>\n",text_x, text_y, exonNum));
        }
    }


    private void writeDomains(Transcript transcript, int ypos, Writer writer) throws IOException {
        List<DisplayInterproAnnotation> hits = this.annotatedGene
                .getTranscriptToInterproHitMap()
                .getOrDefault(transcript.accessionId(), new ArrayList<>());
        for (DisplayInterproAnnotation hit : hits) {
            if (hit.isFamily() || hit.isSuperFamily()) {
                continue; // do not show families
            }
            double xstart = translateProteinToSvgCoordinate(hit.getStart());
            double xend = translateProteinToSvgCoordinate(hit.getEnd());
            double width = xend - xstart;
            String color = this.interproEntryColorMap.get(hit.getInterproEntry());
            double Y = ypos - 0.5 * ISOFORM_HEIGHT;

            if (hit.isSite()) {
                double Y2 = Y-2;
                double Ytop = Y2-0.25*ISOFORM_HEIGHT;
                double Xmiddle = xstart + 0.5*width;
                double Xend = xstart + width;
                System.out.println("SITE-" + hit.getInterproEntry().getDescription() + ":" + hit.getStart() + "-" + hit.getEnd());
                String line = String.format("<line x1=\"%f\" y1=\"%f\" x2=\"%f\" y2=\"%f\" stroke=\"%s\" />",
                Xmiddle, Y, Xmiddle, Y2, color);
                //writer.write(line);
                String triangle = String.format("<polygon points=\"%f,%f %f,%f %f,%f\"\n" +
                        "style=\"fill:%s;stroke:black;stroke-width:1\"/>", Xmiddle, Y2, xstart, Ytop, xend, Ytop, color);
                String rect = String.format("<rect x=\"%f\" y=\"%f\" width=\"%f\" height=\"%f\" " +
                                "style=\"fill:%s;stroke:black;stroke-width:1\" />\n",
                        xstart, Y2, width, 0.25*ISOFORM_HEIGHT, color);
                writer.write(triangle);
            } else {
                String rect = String.format("<rect x=\"%f\" y=\"%f\" width=\"%f\" height=\"%d\" " +
                                "style=\"fill:%s;stroke:black;stroke-width:1\" />\n",
                        xstart, Y, width, ISOFORM_HEIGHT, color);
                writer.write(rect);
            }
        }
    }




    /**
     * Get a string containing an SVG representing the SV.
     *
     * @return an SVG string
     */
    @Override
    public String getSvg() {
        StringWriter swriter = new StringWriter();
        try {
            writeHeader(swriter);
            if (hasVisualizableDomains()) {
                write(swriter);
            } else {
                writeExcuse(swriter);
            }
            writeFooter(swriter);
            return swriter.toString();
        } catch (IOException e) {
            return getSvgErrorMessage(e.getMessage());
        }
    }

    private void writeExcuse(StringWriter swriter) {
        String svg = "<text>" +
                "No protein domains for " + this.annotatedGene.getHbaDealsResult().getSymbol() +
                "  </text>";
        swriter.write(svg);
    }

    private boolean hasVisualizableDomains() {
        return this.annotatedGene.hasInterproAnnotations();
    }


    private void writeIsoform(int ypos, Transcript transcript, double logFC, Writer writer) throws IOException {
        final int EXON_CARTOON_YSKIP = 15;
        double xend = translateProteinToSvgCoordinate(transcript.getProteinLength());

        writeProteinBox(transcript, xend, ypos, logFC, writer);
        writeDomains(transcript, ypos, writer);
        writeExons(transcript, xend, ypos+EXON_CARTOON_YSKIP,writer);
    }

    /**
     * Wirte an SVG (without header) representing this SV. Not intended to be used to create a stand-alone
     * SVG (for this, user {@link #getSvg()}
     *
     * @param writer a file handle
     */
    @Override
    public void write(Writer writer) {
        int y = 40;
        try {
            Map<Transcript, Double> upreg = annotatedGene.getUpregulatedExpressedTranscripts();
            Map<Transcript, Double> downreg = annotatedGene.getDownregulatedExpressedTranscripts();
            writer.write(SvgUtil.text(20,y, BLACK, 24, "Upregulated:"));
            y+= 40;
            for (var e : upreg.entrySet()) {
                Transcript transcript = e.getKey();
                double logFC = e.getValue();
                if (transcript.isCoding()) {
                    writeIsoform(y, transcript, logFC, writer);
                    y += HEIGHT_PER_DISPLAY_ITEM;
                }
            }
            y+= 20;
            writer.write(SvgUtil.text(20,y, BLACK, 24, "Downregulated:"));
            y+= 40;
            for (var e : downreg.entrySet()) {
                Transcript transcript = e.getKey();
                double logFC = e.getValue();
                if (transcript.isCoding()) {
                    writeIsoform(y, transcript, logFC, writer);
                    y += HEIGHT_PER_DISPLAY_ITEM;
                }
            }
            writeInterproLabelsWithColorBoxes(writer, y);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void writeInterproLabelsWithColorBoxes(Writer writer, double Y) throws IOException {
        for (var entry : interproEntryColorMap.entrySet()) {
            InterproEntry accession = entry.getKey();
            String label = String.format("%s (%s)", accession.getDescription(), entry.getKey().getIntroproAccession());
            String color = entry.getValue();
            int boxDimension = 20;
            double startx = 50;
            writer.write(SvgUtil.square(startx, Y, boxDimension, color));
            double x = startx + 2 * boxDimension;
            double textY = Y + 0.9*boxDimension;
            writer.write(SvgUtil.text(x, textY, BLACK, 24, label));
            Y += HEIGHT_PER_INTERPRO_ROW;
        }
    }


    public static AbstractSvgGenerator factory(AnnotatedGene annotatedGene) {
        int height = HEIGHT_PER_DISPLAY_ITEM * annotatedGene.getCodingTranscriptCount() + HEIGHT_FOR_SV_DISPLAY;
        Map<AccessionNumber, List<DisplayInterproAnnotation>> interpromap = annotatedGene.getTranscriptToInterproHitMap();
        SortedMap<InterproEntry, String>  interproEntryColorMap = new TreeMap<>();
        List<InterproEntry> entryList = interpromap
                .values()
                .stream()
                .flatMap(Collection::stream)
                .map(DisplayInterproAnnotation::getInterproEntry)
                .filter(Predicate.not(InterproEntry::isFamilyOrSuperfamily))
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        Random random = new Random();
        int i = random.nextInt(colors.length);
        for (var entry : entryList) {
            interproEntryColorMap.put(entry,String.format("%s",colors[i]));
            i = i + 1 == colors.length ? 0 : i + 1;
        }
        int n = interproEntryColorMap.size();
        n = n%2 == 0 ? n : n+1;
        height += n * HEIGHT_PER_INTERPRO_ROW;
        return new ProteinSvgGenerator(height, annotatedGene, interproEntryColorMap);
    }

    public static String empty(String symbol) {
        return  "<svg width=\"" + SVG_WIDTH + "\" height=\"" + 50+ "\" \nxmlns=\\\"http://www.w3.org/2000/svg\\\" \" +\n" +
                "                        \"xmlns:svg=\\\"http://www.w3.org/2000/svg\\\">\\n" +
                "<style> text { font: 14px; } </style>\n" +
                "<text x=\"20\" y=\"20\">" +
                "No protein domains available for " + symbol +
                ".</text>\n</svg>";
    }



}
