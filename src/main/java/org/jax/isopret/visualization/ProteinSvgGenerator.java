package org.jax.isopret.visualization;


import org.jax.isopret.prosite.PrositeHit;
import org.jax.isopret.transcript.AnnotatedGene;
import org.jax.isopret.transcript.Transcript;


import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.*;


public class ProteinSvgGenerator extends AbstractSvgGenerator {
    static final int SVG_WIDTH = 1400;
    static final int HEIGHT_FOR_SV_DISPLAY = 100;
    static final int HEIGHT_PER_DISPLAY_ITEM = 60;
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

    private final int minProteinLength = 0;
    private final int maxProteinLength;

    private final int proteinMinSvgPos;
    private final int proteinMaxSvgPos;
    private final int keyMinSvgPos;
    private final int keyMaxSvgPos;

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
     * @param writer file handle
     * @throws IOException if we cannot write the box
     */
    protected void writeProteinBox(Transcript transcript, int ypos, Writer writer) throws IOException {
        double xstart = this.proteinMinSvgPos;
        double xend = translateProteinToSvgCoordinate(transcript.getProteinLength());
        double width = xend - xstart;
        double Y = ypos - 0.5 * ISOFORM_HEIGHT;
        String rect = String.format("<rect x=\"%f\" y=\"%f\" width=\"%f\" height=\"%d\" " +
                        "style=\"stroke:%s; fill:none \" />\n",
                xstart, Y, width, ISOFORM_HEIGHT, BLACK);
        writer.write(rect);
        double midishpoint = this.proteinMinSvgPos + 0.4 * (this.proteinMaxSvgPos - this.proteinMinSvgPos);
        Y = ypos - 0.55 * ISOFORM_HEIGHT;
        String label = String.format("<text x=\"%f\" y=\"%f\" fill=\"%s\">%s</text>\n",
                midishpoint, Y, PURPLE, transcript.accessionId());
        writer.write(label);
    }


    private void writeDomains(Transcript transcript, int ypos, Writer writer) throws IOException {
        List<PrositeHit> hits = this.annotatedGene.getPrositeHitMap().getOrDefault(transcript.getAccessionIdNoVersion(), new ArrayList<>());
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
        int starty = 20;
        int y = starty;
        int startx = this.keyMinSvgPos;
        String svg = String.format("<text x=\"%d\" y=\"%d\" fill=\"%s\">Domains and Motifs</text>\n",
                startx, y, BLACK);
        writer.write(svg);
        y += 100;
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


    private void writeIsoform(int ypos, Transcript transcript, Writer writer) throws IOException {
        writeProteinBox(transcript, ypos, writer);
        writeDomains(transcript, ypos, writer);
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
        int starty = 70;
        int y = starty;
        try {
            for (var transcript : this.expressedTranscriptList) {
                if (transcript.isCoding()) {
                    writeIsoform(y, transcript, writer);
                } else {
                    writeNoncodingIsoform(y, transcript, writer);
                }
                y += HEIGHT_PER_DISPLAY_ITEM;
            }
            writeKey(writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static AbstractSvgGenerator factory(AnnotatedGene annotatedTranscript, Map<String, String> id2nameMap) {
        List<Transcript> affectedTranscripts = annotatedTranscript.getExpressedTranscripts();
        int height = HEIGHT_PER_DISPLAY_ITEM * affectedTranscripts.size() + HEIGHT_FOR_SV_DISPLAY;
        return new ProteinSvgGenerator(height,
                annotatedTranscript,
                id2nameMap);
    }
}
