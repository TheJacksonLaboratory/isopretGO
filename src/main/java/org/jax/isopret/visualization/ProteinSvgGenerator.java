package org.jax.isopret.visualization;

import org.jax.isopret.hbadeals.HbaDealsResult;
import org.jax.isopret.hbadeals.HbaDealsTranscriptResult;
import org.jax.isopret.prosite.PrositeHit;
import org.jax.isopret.transcript.AnnotatedGene;
import org.jax.isopret.transcript.Transcript;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ProteinSvgGenerator extends AbstractSvgGenerator {
    static final int SVG_WIDTH = 1400;
    static final int HEIGHT_FOR_SV_DISPLAY = 200;
    static final int HEIGHT_PER_DISPLAY_ITEM = 80;


    private final Map<String, HbaDealsTranscriptResult> results;

    private ProteinSvgGenerator(int height, List<Transcript> affected, HbaDealsResult result, Map<String, List<PrositeHit>> hitmap) {
        super(SVG_WIDTH,height);
        // get prosite data if possible
        this.results = result.getTranscriptMap();

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



    public static AbstractSvgGenerator factory(AnnotatedGene annotatedTranscript) {
        List<Transcript> transcripts = annotatedTranscript.getTranscripts();
        HbaDealsResult result = annotatedTranscript.getHbaDealsResult();
        Map<String, HbaDealsTranscriptResult> transcriptMap = result.getTranscriptMap();
        List<Transcript> affectedTranscripts = transcripts
                .stream()
                .filter(t -> transcriptMap.containsKey(t.getAccessionIdNoVersion()))
                .collect(Collectors.toList());
        int height = HEIGHT_PER_DISPLAY_ITEM * affectedTranscripts.size() + HEIGHT_FOR_SV_DISPLAY;
        return new ProteinSvgGenerator(height,
                affectedTranscripts,
                annotatedTranscript.getHbaDealsResult(),
                annotatedTranscript.getPrositeHitMap());
    }
}
