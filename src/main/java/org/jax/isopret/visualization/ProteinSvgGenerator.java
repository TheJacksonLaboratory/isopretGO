package org.jax.isopret.visualization;

import org.jax.isopret.except.IsopretRuntimeException;
import org.jax.isopret.hbadeals.HbaDealsResult;
import org.jax.isopret.hbadeals.HbaDealsTranscriptResult;
import org.jax.isopret.prosite.PrositeHit;
import org.jax.isopret.transcript.AnnotatedGene;
import org.jax.isopret.transcript.Transcript;
import org.monarchinitiative.variant.api.GenomicRegion;
import org.monarchinitiative.variant.api.Strand;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.*;
import java.util.stream.Collectors;

public class ProteinSvgGenerator extends AbstractSvgGenerator {
    static final int SVG_WIDTH = 1400;
    static final int HEIGHT_FOR_SV_DISPLAY = 200;
    static final int HEIGHT_PER_DISPLAY_ITEM = 80;
    private final static String colors[] = {"F08080", "CCE5FF", "ABEBC6", "FFA07A", "C39BD3", "FEA6FF","F7DC6F", "CFFF98", "A1D6E2",
            "EC96A4", "E6DF44", "F76FDA","FFCCE5", "E4EA8C", "F1F76F", "FDD2D6", "F76F7F", "DAF7A6","FFC300" ,"F76FF5" , "FFFF99",
            "FF99FF", "99FFFF","CCFF99","FFE5CC","FFD700","9ACD32","7FFFD4","FFB6C1","FFFACD",
            "FFE4E1","F0FFF0","F0FFFF"};

    private final Map<String, HbaDealsTranscriptResult> results;

    private final Map<String, List<PrositeHit>> prositeHitMap;

    private final List<Transcript> expressedTranscriptList;

    private final Map<String,String> prositeId2nameMap;
    /** Map of the prosite IDs and name that we actually used. */
    private final  SortedMap<String, String> sortedPrositeIdMap;

    private final Map<String, String> prositeIdToColorMap;

    private ProteinSvgGenerator(int height, AnnotatedGene annotatedGene, Map<String,String> id2nameMap) {
        super(SVG_WIDTH,height);
        // get prosite data if possible
        this.results = annotatedGene.getHbaDealsResult().getTranscriptMap();
        this.prositeHitMap = annotatedGene.getPrositeHitMap();
        this.prositeId2nameMap = id2nameMap;
        this.expressedTranscriptList = annotatedGene.getExpressedTranscripts();
        this.sortedPrositeIdMap = new TreeMap<>();
        for (var hitlist : prositeHitMap.values()) {
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
            i = i+1 == colors.length ? 0 : i+1;
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
           // write(swriter);
            writeFooter(swriter);
            return swriter.toString();
        } catch (IOException e) {
            return getSvgErrorMessage(e.getMessage());
        }
    }

    /**
     * Wirte an SVG (without header) representing this SV. Not intended to be used to create a stand-alone
     * SVG (for this, user {@link #getSvg()}
     *
     * @param writer a file handle
     * @throws IOException if we cannot write.
     */
    @Override
    public void write(Writer writer)  {
        int starty = 50;
        int y = starty;
        // TODO WRITE SYMBOLS TO DESCRIBE THE ALTERNATE SPLICING
        y += 100;
//        try {
//            for (var tmod : this.affectedTranscripts) {
//                writeTranscript(tmod, y, writer);
//                y += HEIGHT_PER_DISPLAY_ITEM;
//            }
//            writeScale(writer, y);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }



    public static AbstractSvgGenerator factory(AnnotatedGene annotatedTranscript, Map<String,String> id2nameMap) {
        List<Transcript> affectedTranscripts = annotatedTranscript.getExpressedTranscripts();
        int height = HEIGHT_PER_DISPLAY_ITEM * affectedTranscripts.size() + HEIGHT_FOR_SV_DISPLAY;
        return new ProteinSvgGenerator(height,
                annotatedTranscript,
                id2nameMap);
    }
}
