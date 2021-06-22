package org.jax.isopret.visualization;

import org.jax.isopret.go.GoTermIdPlusLabel;
import org.jax.isopret.hbadeals.HbaDealsResult;
import org.jax.isopret.hbadeals.HbaDealsTranscriptResult;
import org.jax.isopret.interpro.DisplayInterproAnnotation;
import org.jax.isopret.transcript.AccessionNumber;
import org.jax.isopret.transcript.AnnotatedGene;
import org.jax.isopret.transcript.Transcript;
import org.monarchinitiative.svart.Contig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.*;

/**
 * This class acts as an interface between the HtmlVisualizer and the analysis results and
 * should be used if the input data represent Ensembl ids.
 * @author Peter N Robinson
 */
public class EnsemblVisualizable implements Visualizable {
    private static final Logger LOGGER = LoggerFactory.getLogger(EnsemblVisualizable.class);

     /** Number of all annotated transcripts of some gene */
    private final int totalTranscriptCount;

    /** All annotated transcripts of some gene that were expressed according to HBA deals */
    private final List<Transcript> expressedTranscripts;

    private final HbaDealsResult hbaDealsResult;

    private final String chromosome;

    private final AnnotatedGene agene;

    private final List<GoTermIdPlusLabel> goterms;

    private final boolean differentiallyExpressed;

    private final boolean differentiallySpliced;

    private final double splicingThreshold;

    private final int i;

    public EnsemblVisualizable(AnnotatedGene agene, Set<GoTermIdPlusLabel> goterms, int i) {
        this.agene = agene;
        List<GoTermIdPlusLabel> golist = new ArrayList<>(goterms);
        Collections.sort(golist);
        this.goterms = golist;
        this.totalTranscriptCount = agene.getTranscripts().size();
        this.expressedTranscripts = agene.getExpressedTranscripts();
        this.hbaDealsResult = agene.getHbaDealsResult();
        String chr = this.expressedTranscripts.stream().map(Transcript::contig).map(Contig::name).findAny().orElse("n/a");
        this.chromosome = chr.startsWith("chr") ? chr : "chr" + chr;
        this.differentiallyExpressed = agene.passesExpressionThreshold();
        this.differentiallySpliced = agene.passesSplicingThreshold();
        this.splicingThreshold = agene.getSplicingThreshold();
        this.i = i;
    }

    public int getI() { return i; }

    @Override
    public String getGeneSymbol() {
        return hbaDealsResult.getSymbol();
    }

    @Override
    public String getGeneAccession() {
        return this.hbaDealsResult.getGeneAccession().getAccessionString();
    }

    private String getEnsemblUrl(String accession) {
        return String.format("https://ensembl.org/Homo_sapiens/Gene/Summary?db=core;g=%s", accession);
    }

    private String getEnsemblTranscriptUrl(String accession) {
        //https://useast.ensembl.org/Homo_sapiens/Transcript/Summary?db=core;g=ENSG00000181026;r=15:88626612-88632281;t=ENST00000557927
        return String.format("https://ensembl.org/Homo_sapiens/Gene/Summary?db=core;t=%s", accession);
    }

    private String getHtmlAnchor(String accession) {
        String url = getEnsemblUrl(accession);
        return String.format("<a href=\"%s\" target=\"__blank\">%s</a>\n", url, accession);
    }

    @Override
    public int getTotalTranscriptCount() {
        return this.totalTranscriptCount;
    }

    @Override
    public int getExpressedTranscriptCount() {
        return this.expressedTranscripts.size();
    }

    @Override
    public String getGeneUrl() {
        return getEnsemblUrl(getGeneAccession());
    }

    @Override
    public String getChromosome() {
        return this.chromosome;
    }

    @Override
    public double getExpressionPval() {
        return this.hbaDealsResult.getExpressionP();
    }

    @Override
    public double getMostSignificantSplicingPval() {
        return this.hbaDealsResult.getSmallestSplicingP();
    }

    @Override
    public double getExpressionFoldChange() {
        double logFc = getExpressionLogFoldChange();
        return Math.exp(logFc);
    }

    @Override
    public double getExpressionLogFoldChange() {
        return this.hbaDealsResult.getExpressionFoldChange();
    }

    @Override
    public String getIsoformSvg() {
        AbstractSvgGenerator svggen = TranscriptSvgGenerator.factory(agene);
        return svggen.getSvg();
    }

    @Override
    public String getProteinSvg() {
        try {
            // Return a message only if we cannot find prosite domains.
           // if (agene.getPrositeHitMap().isEmpty()) {
           if (! agene.hasInterproAnnotations()) {
                return ProteinSvgGenerator.empty(agene.getHbaDealsResult().getSymbol());
            }
            AbstractSvgGenerator svggen = ProteinSvgGenerator.factory(agene);
            return svggen.getSvg();
        } catch (Exception e) {
            LOGGER.error("Could not generate protein SVG: {}", e.getMessage());
            return "<p>Could not generate protein SVG because: " + e.getMessage() + "</p>";
        }
    }

    private List<String> getIsoformRow(HbaDealsTranscriptResult transcriptResult) {
        List<String> row = new ArrayList<>();
        String url = getEnsemblTranscriptUrl(transcriptResult.getTranscript());
        String a =  String.format("<a href=\"%s\" target=\"__blank\">%s</a>\n", url, transcriptResult.getTranscript());
        row.add(a);
        row.add(String.format("%.3f",transcriptResult.getLog2FoldChange()));
        String prob = String.format("%.2f", transcriptResult.getP()) + (transcriptResult.getP() <= splicingThreshold ? " (*)" : "");
        row.add(prob);
        return row;
    }

    /**
     * Return data to show the isoforms.
     * We have isoform accession number as a HTML link, followed by isoform log fold change and isoform p value and
     * corrected P value, that is for M rows, we return an M*4 matrix of strings intended to build an HTML table
     * @return a matrix of data representing the contents of an HTML table for the isoforms
     */
    @Override
    public List<List<String>> getIsoformTableData() {
        List<List<String>> rows = new ArrayList<>();
        Map<AccessionNumber, HbaDealsTranscriptResult> transcriptMap = this.hbaDealsResult.getTranscriptMap();
        for (Transcript transcript : this.expressedTranscripts) {
            if (transcriptMap.containsKey(transcript.accessionId())) {
                HbaDealsTranscriptResult transcriptResult = transcriptMap.get(transcript.accessionId());
                var row = getIsoformRow(transcriptResult);
                rows.add(row);
            }
        }
        return rows;
    }

    /**
     * @return a list of interpro annotations that cover the isoforms of the gene that are expressed in our data
     */
    @Override
    public List<DisplayInterproAnnotation> getInterproForExpressedTranscripts() {
        Map<AccessionNumber, List<DisplayInterproAnnotation>> interproMap = this.agene.getTranscriptToInterproHitMap();
        Set<DisplayInterproAnnotation> interpro = new HashSet<>();
        for (AccessionNumber transcriptId : this.hbaDealsResult.getTranscriptMap().keySet()) {
            if (interproMap.containsKey(transcriptId)) {
                interpro.addAll(interproMap.get(transcriptId));
            }
        }
        List<DisplayInterproAnnotation> sortedList = new ArrayList<>(interpro);
        Collections.sort(sortedList);
        return sortedList;
    }

    @Override
    public List<GoTermIdPlusLabel> getGoTerms() {
        return this.goterms;
    }

    @Override
    public boolean isDifferentiallyExpressed() {
        return this.differentiallyExpressed;
    }

    @Override
    public boolean isDifferentiallySpliced(){
        return this.differentiallySpliced;
    }
}
