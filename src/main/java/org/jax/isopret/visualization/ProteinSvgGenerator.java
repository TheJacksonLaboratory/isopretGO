package org.jax.isopret.visualization;


import org.jax.isopret.prosite.PrositeHit;
import org.jax.isopret.transcript.AnnotatedGene;
import org.jax.isopret.transcript.Transcript;


import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.*;

/**
 * Write an SVG representing the protein domains and corresponding exon structure.
 * @author Peter N Robinson
 */
public class ProteinSvgGenerator extends AbstractSvgGenerator {
    static final int SVG_WIDTH = 1400;
    static final int HEIGHT_FOR_SV_DISPLAY = 120;
    static final int HEIGHT_PER_DISPLAY_ITEM = 90;
    private static final int ISOFORM_HEIGHT = 20;
    private final static String[] colors = {"F08080", "CCE5FF", "ABEBC6", "FFA07A", "C39BD3", "FEA6FF", "F7DC6F", "CFFF98", "A1D6E2",
            "EC96A4", "E6DF44", "F76FDA", "FFCCE5", "E4EA8C", "F1F76F", "FDD2D6", "F76F7F", "DAF7A6", "FFC300", "F76FF5", "FFFF99",
            "FF99FF", "99FFFF", "CCFF99", "FFE5CC", "FFD700", "9ACD32", "7FFFD4", "FFB6C1", "FFFACD",
            "FFE4E1", "F0FFF0", "F0FFFF"};

   private final List<Transcript> expressedTranscriptList;

    private final Map<String, String> prositeId2nameMap;
    /**
     * Map of the prosite IDs and name that we actually used.
     */
    private final SortedMap<String, String> sortedPrositeIdMap;

    private final Map<String, String> prositeIdToColorMap;

    private final AnnotatedGene annotatedGene;

    private final int maxProteinLength;

    private final int proteinMinSvgPos;
    private final int proteinMaxSvgPos;
    private final int keyMinSvgPos;
    private final int keyMaxSvgPos;

    private final static String JAMA_BLUE = "00b2e2";

    private enum Regulation {UP, DOWN}


    private ProteinSvgGenerator(int height, AnnotatedGene annotatedGene, Map<String, String> id2nameMap) {
        super(SVG_WIDTH, height);
        // get prosite data if possible
        this.annotatedGene = annotatedGene;
        this.prositeId2nameMap = id2nameMap;
        this.expressedTranscriptList = annotatedGene.getExpressedTranscripts();
        this.sortedPrositeIdMap = new TreeMap<>();
        for (var hitlist : this.annotatedGene.getPrositeHitMap().values()) {
            for (var hit : hitlist) {
                String id = hit.getAccession();
                String label = this.prositeId2nameMap.getOrDefault(id, id); // if we cannot find the label, just use the id
                this.sortedPrositeIdMap.put(id, label);
            }
        }
        // get colors -- start from a random index
        prositeIdToColorMap = new HashMap<>();
        Random random = new Random();
        int i = random.nextInt(colors.length);
        for (var id : sortedPrositeIdMap.keySet()) {
            prositeIdToColorMap.put(id, colors[i]);
            i = i + 1 == colors.length ? 0 : i + 1;
        }
        this.maxProteinLength = this.expressedTranscriptList
                .stream()
                .map(Transcript::getProteinLength)
                .max(Integer::compareTo)
                .orElse(1000);
        this.proteinMinSvgPos = 50;
        this.proteinMaxSvgPos = 1000;
        this.keyMinSvgPos = 1100;
        this.keyMaxSvgPos = 1400;
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
     * Write a box for the entire transcript
     *
     * @param ypos SVG Y coordinate to write the protein box
     * @param xend end popsition of this protein in SVG coordinates.
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
        double midishpoint = this.proteinMinSvgPos + 0.4 * (this.proteinMaxSvgPos - this.proteinMinSvgPos);
        Y = ypos - 0.55 * ISOFORM_HEIGHT;
        String label = String.format("<text x=\"%f\" y=\"%f\" fill=\"%s\">%s (log-fold change: %.2f)</text>\n",
                xstart+5, Y-5, PURPLE, transcript.accessionId(), logFC);
        writer.write(label);
    }

    private void writeExons(Transcript transcript, double xend, int ypos, Writer writer) throws IOException{
        double xstart = this.proteinMinSvgPos;
        double width = xend - xstart;
        List<Integer> codingExonLengths = transcript.codingExonLengths();
        int cdnalen = codingExonLengths.stream().mapToInt(Integer::intValue).sum();
        double widthPerBp = width/cdnalen;
        int i = 0;
        double x1 = xstart;
        for (int cdsLen : codingExonLengths) {
            i++;
            if (cdsLen == 0) continue;
            double exonwidth =  cdsLen * widthPerBp;
            writeExon(x1,exonwidth,i, ypos, writer);
            x1 += exonwidth;
        }
    }

    private void writeExon(double x1, double exonwidth , int exonNum, int ypos, Writer writer) throws IOException {
        String rect = String.format("<rect x=\"%f\" y=\"%d\" width=\"%f\" height=\"%d\" " +
                        "style=\"fill:#%s;stroke:black;stroke-width:1\" />\n",
                x1, ypos, exonwidth, ISOFORM_HEIGHT, JAMA_BLUE);
        writer.write(rect);
        final double digitWidth = 4;
        if (exonwidth > 20) {
            double text_x = x1 + exonwidth/2;
            text_x -= exonNum>9 ? 2*digitWidth : digitWidth;
            double text_y = ypos + 0.75*ISOFORM_HEIGHT;
            writer.write(String.format("<text x=\"%f\" y=\"%f\" fill=\"white\">%d</text>\n",text_x, text_y, exonNum));
        }
    }


    private void writeDomains(Transcript transcript, int ypos, Writer writer) throws IOException {
        List<PrositeHit> hits = this.annotatedGene.getPrositeHitMap().getOrDefault(transcript.accessionId().getAccessionString(), new ArrayList<>());
        for (PrositeHit hit : hits) {
            String color = this.prositeIdToColorMap.get(hit.getAccession());
            double xstart = translateProteinToSvgCoordinate(hit.getStartAminoAcidPos());
            double xend = translateProteinToSvgCoordinate(hit.getEndAminoAcidPos());
            double width = xend - xstart;
            double Y = ypos - 0.5 * ISOFORM_HEIGHT;
            String rect = String.format("<rect x=\"%f\" y=\"%f\" width=\"%f\" height=\"%d\" " +
                            "style=\"fill:#%s;stroke:black;stroke-width:1\" />\n",
                    xstart, Y, width, ISOFORM_HEIGHT, color);
            writer.write(rect);
        }
    }

    private void writeKey(Writer writer) throws IOException {
        int y = 20;
        int startx = this.keyMinSvgPos;
        String svg = String.format("<text x=\"%d\" y=\"%d\" fill=\"%s\">Domains and Motifs</text>\n",
                startx, y, BLACK);
        writer.write(svg);
        y += 40;
        int Y_INTERVAL = 60;
        for (var entry : sortedPrositeIdMap.entrySet()) {
            String accession = entry.getKey();
            String label = entry.getValue();
            String color = this.prositeIdToColorMap.get(accession);
            int boxDimension = 20;
            String rect = String.format("<rect x=\"%d\" y=\"%d\" width=\"%d\" height=\"%d\" " +
                            "style=\"fill:#%s;stroke:black;stroke-width:1\" />\n",
                    startx, y, boxDimension,boxDimension, color);
            writer.write(rect);
            int x = startx + 2*boxDimension;
            int ybox = (int)(y + 0.5*boxDimension);
            svg = String.format("<text x=\"%d\" y=\"%d\" fill=\"%s\">%s</text>\n",
                    x, ybox, BLACK, label);
            writer.write(svg);
            ybox += boxDimension;
            svg = String.format("<text x=\"%d\" y=\"%d\" fill=\"%s\">%s</text>\n",
                    x, ybox, BLACK, accession);
            writer.write(svg);
            y += Y_INTERVAL;
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
        return ! this.annotatedGene.getPrositeHitMap().isEmpty();
    }


    private void writeIsoform(int ypos, Transcript transcript, double logFC, Writer writer) throws IOException {
        final int EXON_CARTOON_YSKIP = 15;
        double xend = translateProteinToSvgCoordinate(transcript.getProteinLength());

        writeProteinBox(transcript, xend, ypos, logFC, writer);
        writeDomains(transcript, ypos, writer);
        writeExons(transcript, xend, ypos+EXON_CARTOON_YSKIP,writer);
    }

    private void writeNoncodingIsoform(int ypos, Transcript transcript, Writer writer) throws IOException {
        int inset = 50;
        String svg = String.format("<g fill=\"none\" stroke=\"" + LIGHT_GREY + "\" stroke-width=\"2\">\n" +
                "    <path stroke-dasharray=\"5,5\" d=\"M%d %d L%d %d\" />\n" +
                "  </g>", this.proteinMinSvgPos + inset, ypos, this.proteinMaxSvgPos - inset, ypos);
        writer.write(svg);
        double midishpoint = this.proteinMinSvgPos + 0.4 * (this.proteinMaxSvgPos - this.proteinMinSvgPos);
        svg = String.format("<text x=\"%f\" y=\"%f\" fill=\"%s\">%s (non-coding)</text>\n",
                midishpoint, (double) ypos, PURPLE, transcript.accessionId());
        writer.write(svg);
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
            writer.write(String.format("<text x=\"%d\" y=\"%d\">Upregulated:</text>\n", 20, y));
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
            writer.write(String.format("<text x=\"%d\" y=\"%d\">Downregulated:</text>\n", 20, y));
            y+= 40;
            for (var e : downreg.entrySet()) {
                Transcript transcript = e.getKey();
                double logFC = e.getValue();
                if (transcript.isCoding()) {
                    writeIsoform(y, transcript, logFC, writer);
                    y += HEIGHT_PER_DISPLAY_ITEM;
                }
            }

            int nonCodingTranscripts = this.annotatedGene.getNoncodingTranscriptCount();
            if (nonCodingTranscripts>0) {
                int xpos = this.proteinMinSvgPos + 10;
                writer.write(String.format("<text x=\"%d\" y=\"%d\">%s has %d non-coding %s with non-zero read counts</text>\n",
                        xpos, y,
                        this.annotatedGene.getHbaDealsResult().getSymbol(),
                        nonCodingTranscripts,
                        nonCodingTranscripts>1 ? "transcripts" : "transcript"));
            }
            writeKey(writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static AbstractSvgGenerator factory(AnnotatedGene annotatedGene, Map<String, String> id2nameMap) {
        int height = HEIGHT_PER_DISPLAY_ITEM * annotatedGene.getCodingTranscriptCount() + HEIGHT_FOR_SV_DISPLAY;
        if (annotatedGene.getNoncodingTranscriptCount()>0) {
            height += HEIGHT_FOR_SV_DISPLAY;
        }
        return new ProteinSvgGenerator(height,
                annotatedGene,
                id2nameMap);
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
