package org.jax.isopret.visualization;


import org.jax.isopret.model.DisplayInterproAnnotation;
import org.jax.isopret.model.InterproEntry;
import org.jax.isopret.model.AccessionNumber;
import org.jax.isopret.model.AnnotatedGene;
import org.jax.isopret.model.Transcript;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.*;
import java.util.function.Predicate;

/**
 * Write an SVG representing the protein domains and corresponding exon structure.
 * @author Peter N Robinson
 */
public class ProteinSvgGenerator extends AbstractSvgGenerator {
    final Logger LOGGER = LoggerFactory.getLogger(ProteinSvgGenerator.class);
    private static final int SVG_WIDTH = 1050;
    private static final int HEIGHT_FOR_SV_DISPLAY = 160;
    private static final int HEIGHT_PER_DISPLAY_ITEM = 90;
    private static final int ISOFORM_HEIGHT = 20;
    /** 7 colors from the star trek palette from the R ggsci library. */
    private static final String [] starTrekColors = {"#CC0C00", "#5C88DA", "#84BD00", "#FFCD00", "#7C878E", "#00B5E2", "#00AF66"};
    /** 8 colors from the NEJM palette from the R ggsci library. */
    private static final String [] nejmColors = {"#BC3C29", "#0072B5", "#E18727", "#20854E", "#7876B1", "#6F99AD", "#FFDC91", "#EE4C97"};
    /** 9 colors from the chicago palette from the R ggsci library. */
    private static final String [] uchicagoColors = {"#800000", "#767676", "#FFA319", "#8A9045", "#155F83", "#C16622", "#8F3931", "#58593F", "#350E20"};
    /** 10 colors from the NPG palette from the R ggsci library. */
    private static final String [] npgColors = {"#E64B35", "#4DBBD5", "#00A087", "#3C5488", "#F39B7F" ,"#8491B4", "#91D1C2", "#DC0000", "#7E6148", "#B09C85" };
    /** 10 colors from the AAAS palette from the R ggsci library. */
    private static final String [] aaasColors = { "#3B4992", "#EE0000", "#008B45", "#631879", "#008280", "#BB0021", "#5F559B", "#A20056", "#808180", "#1B1919"};
    /** 10 colors from the d3 palette from the R ggsci library. */
    private static final String [] d3colors = {"#1F77B4", "#FF7F0E", "#2CA02C", "#D62728", "#9467BD", "#8C564B", "#E377C2", "#7F7F7F", "#BCBD22", "#17BECF"};
    /** 26 colors from the UCSC palette from the R ggsci library. */
    private static final String [] ucscColors =
    {"#FF0000", "#FF9900", "#FFCC00", "#00FF00", "#6699FF", "#CC33FF",
             "#99991E", "#999999", "#FF00CC", "#CC0000", "#FFCCCC", "#FFFF00",
             "#CCFF00", "#358000", "#0000CC", "#99CCFF", "#00FFFF", "#CCFFFF",
            "#9900CC", "#CC99FF", "#996600", "#666600", "#666666", "#CCCCCC",
            "#79CC3D", "#CCCC99"};



    private final SortedMap<InterproEntry, String> interproEntryColorMap;

    private final AnnotatedGene annotatedGene;

    private final int maxProteinLength;

    private final int proteinMinSvgPos;
    private final int proteinMaxSvgPos;
    private final int keyMinSvgPos;
    /** Color used for exon boxes. */
    private final static String JAMA_BLUE = "#00b2e2";

    private final static int HEIGHT_PER_INTERPRO_ROW = 30;
    private final static int HEIGHT_PER_INTERPRO_LABELROW = 20;

    private final Set<InterproEntry> siteSet;

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
        siteSet = new HashSet<>();
        for (var hitlist : annotatedGene.getTranscriptToInterproHitMap().values()) {
            for (var hit : hitlist) {
                if (hit.isSite()) {
                    siteSet.add(hit.getInterproEntry());
                }
            }
        }
    }


    /**
     * Transform an amino-acid coordinate to an SVG X coordinate
     *
     * @return coordinate of a given protein amino acid position in SVG space
     */
    private double translateProteinToSvgCoordinate(int aminoAcidPosition) {
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
    private void writeProteinBox(Transcript transcript, double xend, int ypos, double logFC, Writer writer) throws IOException {
        double xstart = this.proteinMinSvgPos;
        double width = xend - xstart;
        double Y = ypos - 0.5 * ISOFORM_HEIGHT;
        writer.write(SvgUtil.unfilledBox(xstart, Y, width, ISOFORM_HEIGHT));
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


    private static final Comparator<DisplayInterproAnnotation> COMPARATOR =
            Comparator.comparingInt(DisplayInterproAnnotation::getStart)
                    .thenComparingInt(DisplayInterproAnnotation::getEnd);
    /** check if two adjacent intervals overlap by at least 95% */



    private void writeDomains(Transcript transcript, int ypos, Writer writer) throws IOException {
        List<DisplayInterproAnnotation> hits = this.annotatedGene
                .getTranscriptToInterproHitMap()
                .getOrDefault(transcript.accessionId(), new ArrayList<>());
        // search for overlapping/redundant hits and keep only one hit
        // this is needed because in some cases we get multiple predictions that differ by only a few amino acids
        Map<String, List<DisplayInterproAnnotation>> hitMap = new HashMap<>();
        for (DisplayInterproAnnotation hit : hits) {
            if (hit.isFamily() || hit.isSuperFamily()) {
                continue; // do not show families
            }
            String accession = hit.getInterproEntry().getIntroproAccession();
            hitMap.putIfAbsent(accession, new ArrayList<>());
            hitMap.get(accession).add(hit);
        }
        List<DisplayInterproAnnotation> filteredHits = new ArrayList<>();
        for (List<DisplayInterproAnnotation> diaList : hitMap.values()) {
            if (diaList.isEmpty()) {
                LOGGER.error("Got empty DisplayInterproAnnotation list (should never happen)");
                continue;
            } else if (diaList.size() == 1) {
                filteredHits.add(diaList.get(0));
            }
            diaList.sort(COMPARATOR);
            DisplayInterproAnnotation currentDia = diaList.get(0); // if we get here, we know there are at least two elems
            for (int i=1; i< diaList.size(); i++) {
                DisplayInterproAnnotation nextDia = diaList.get(i);
                if (currentDia.overlapsBy(nextDia)) {
                    currentDia = currentDia.merge(nextDia);
                } else {
                    filteredHits.add(currentDia);
                    currentDia = nextDia;
                }
            }
            filteredHits.add(currentDia);
        }

        for (DisplayInterproAnnotation hit : filteredHits) {
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
                LOGGER.trace("SITE-" + hit.getInterproEntry().getDescription() + ":" + hit.getStart() + "-" + hit.getEnd());
                String triangle = String.format("<polygon points=\"%f,%f %f,%f %f,%f\"\n" +
                        "style=\"fill:%s;stroke:black;stroke-width:1\"/>", Xmiddle, Y2, xstart, Ytop, xend, Ytop, color);
                writer.write(triangle);
            } else {
                String rect = String.format("<rect x=\"%f\" y=\"%f\" width=\"%f\" height=\"%d\" " +
                                "style=\"fill:%s;fill-opacity:0.9;stroke:black;stroke-width:1\" />\n",
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
                "No protein domains for " + this.annotatedGene.getHbaDealsResult().getGeneModel() +
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
        double startx = 50;
        int boxDimension = 12;
        // we want to show the sites at the bottom of the list, so we store them in this map and then output them
        Map<InterproEntry, String> siteMap = new HashMap<>();
        for (var colorMapEntry : interproEntryColorMap.entrySet()) {
            InterproEntry interproEntry = colorMapEntry.getKey();
            String label = String.format("%s (%s)", interproEntry.getDescription(), colorMapEntry.getKey().getIntroproAccession());
            String color = colorMapEntry.getValue();
            if (siteSet.contains(interproEntry)) {
                siteMap.put(interproEntry, color);
            } else {
                writer.write(SvgUtil.square(startx, Y, boxDimension, color));
                double x = startx + 2 * boxDimension;
                double textY = Y + 0.9 * boxDimension;
                writer.write(SvgUtil.text(x, textY, BLACK, 18, label));
                Y += HEIGHT_PER_INTERPRO_LABELROW;
            }
        }
        for (var colorMapEntry : siteMap.entrySet()) {
            InterproEntry interproEntry = colorMapEntry.getKey();
            String label = String.format("%s (%s)", interproEntry.getDescription(), colorMapEntry.getKey().getIntroproAccession());
            String color = colorMapEntry.getValue();
            double Y2 = Y + 4;
            double Ytop = Y2 - 0.25 * ISOFORM_HEIGHT;
            double Xmiddle = startx + 0.5 * boxDimension;
            double Xend = startx + boxDimension;
            String triangle = String.format("<polygon points=\"%f,%f %f,%f %f,%f\"\n" +
                    "style=\"fill:%s;stroke:black;stroke-width:1\"/>", Xmiddle, Y2, startx, Ytop, Xend, Ytop, color);
            writer.write(triangle);
            double x = startx + 2 * boxDimension;
            double textY = Y + 0.9 * boxDimension;
            writer.write(SvgUtil.text(x, textY, BLACK, 18, label));
            Y += HEIGHT_PER_INTERPRO_LABELROW;
        }
    }

    /**
     * Choose a color palette according to the number of items to display
     *  so we have 7:starTrekColors,8:nejmColors,9:uchicagoColors,10:npgColors,10:aaasColors,10:d3colors,26 colors:ucscColors
     * @param n number of elements to be displayed
     * @return list of colors to display
     */
    private static String [] getColors(int n) {
        long currentTime = System.currentTimeMillis();
        Random r1 = new Random(currentTime);
        if (n>10) return ucscColors;
        else if (n>9) {
            // choose one of three tens
            double r = r1.nextDouble();
            if (r<0.33) return npgColors;
            else if (r<0.67) return aaasColors;
            else return d3colors;
        } else if (n>7) {
            // choose one of the five
            double r = r1.nextDouble();
            if (r<0.2) return npgColors;
            else if (r<0.4) return aaasColors;
            else if (r<0.6) return ucscColors;
            else if (r<0.8) return nejmColors;
            else return d3colors;
        } else {
            double r = r1.nextDouble();
            if (r<0.15) return npgColors;
            else if (r<0.3) return aaasColors;
            else if (r<0.45) return ucscColors;
            else if (r<0.6) return nejmColors;
            else if (r<0.8) return starTrekColors;
            else return d3colors;
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
                .toList();
        int n_items = entryList.size();
        String [] colors = getColors(n_items);
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
