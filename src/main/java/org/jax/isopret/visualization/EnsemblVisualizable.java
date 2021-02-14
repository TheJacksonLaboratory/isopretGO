package org.jax.isopret.visualization;

import org.jax.isopret.go.GoTermIdPlusLabel;
import org.jax.isopret.hbadeals.HbaDealsResult;
import org.jax.isopret.hbadeals.HbaDealsTranscriptResult;
import org.jax.isopret.prosite.PrositeHit;
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

    private final Map<String, List<PrositeHit>> transcriptToHitMap;

    private final HbaDealsResult hbaDealsResult;

    private final String chromosome;

    private final AnnotatedGene agene;

    private final List<GoTermIdPlusLabel> goterms;

    private final boolean differentiallyExpressed;

    private final boolean differentiallySpliced;


    public EnsemblVisualizable(AnnotatedGene agene, Set<GoTermIdPlusLabel> goterms) {
        this.agene = agene;
        List<GoTermIdPlusLabel> golist = new ArrayList<>(goterms);
        Collections.sort(golist);
        this.goterms = golist;
        this.totalTranscriptCount = agene.getTranscripts().size();
        this.expressedTranscripts = agene.getExpressedTranscripts();
        this.transcriptToHitMap = agene.getPrositeHitMap();
        this.hbaDealsResult = agene.getHbaDealsResult();
        String chr = this.expressedTranscripts.stream().map(Transcript::contig).map(Contig::name).findAny().orElse("n/a");
        this.chromosome = chr.startsWith("chr") ? chr : "chr" + chr;
        differentiallyExpressed = agene.passesExpressionThreshold();
        differentiallySpliced = agene.passesSplicingThreshold();
    }

    @Override
    public String getGeneSymbol() {
        return hbaDealsResult.getSymbol();
    }

    @Override
    public String getGeneAccession() {
        return this.hbaDealsResult.getGeneAccession();
    }

    private String getEnsemblUrl(String accession) {
        return String.format("https://ensembl.org/Homo_sapiens/Gene/Summary?db=core;g=%s", accession);
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
        return this.hbaDealsResult.getMostSignificantSplicingPval();
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
    public String getProteinSvg(Map<String, String> prositeIdToName) {
        try {
            // Return a message only if we cannot find prosite domains.
            if (agene.getPrositeHitMap().isEmpty()) {
                return ProteinSvgGenerator.empty(agene.getHbaDealsResult().getSymbol());
            }
            AbstractSvgGenerator svggen = ProteinSvgGenerator.factory(agene, prositeIdToName);
            return svggen.getSvg();
        } catch (Exception e) {
            return "<p>Could not generate protein SVG because: " + e.getMessage() + "</p>";
        }
    }

    private List<String> getIsoformRow(HbaDealsTranscriptResult transcriptResult) {
        List<String> row = new ArrayList<>();
        row.add(getHtmlAnchor(transcriptResult.getTranscript()));
        row.add(String.format("%.3f",transcriptResult.getLog2FoldChange()));
        row.add(String.valueOf(transcriptResult.getP()));
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
        Map<String, HbaDealsTranscriptResult> transcriptMap = this.hbaDealsResult.getTranscriptMap();
        for (Transcript transcript : this.expressedTranscripts) {
            if (transcriptMap.containsKey(transcript.getAccessionIdNoVersion())) {
                HbaDealsTranscriptResult transcriptResult = transcriptMap.get(transcript.getAccessionIdNoVersion());
                var row = getIsoformRow(transcriptResult);
                rows.add(row);
            }
        }
        return rows;
    }


    private String getPrositeHtmlAnchor(String id, String label) {
        String url = "https://prosite.expasy.org/cgi-bin/prosite/prosite-search-ac?" + id;
        return "<a href=\"" + url +"\" target=\"__blank\">" + label +"</a>\n";
    }


    @Override
    public List<List<String>> getPrositeModuleLinks(Map<String, String> prositeIdToName) {
        var prositeHitMap = this.agene.getPrositeHitMap();
        SortedMap<String,String> uniqueHitsMap = new TreeMap<>();
        for (List<PrositeHit> hits : prositeHitMap.values()) {
            for (PrositeHit hit : hits) {
                String acc = hit.getAccession();
                if (prositeIdToName.containsKey(acc)) {
                    uniqueHitsMap.putIfAbsent(acc, prositeIdToName.get(acc));
                }
            }
        }
        List<List<String>> prositeLinks = new ArrayList<>();
        for (var entry : uniqueHitsMap.entrySet()) {
            String id = entry.getKey();
            String label = entry.getValue();
            String anchor = getPrositeHtmlAnchor(id, label);
            List<String> row = new ArrayList<>();
            row.add(id);
           // row.add(label);
            row.add(anchor);
            prositeLinks.add(row);
        }
        return prositeLinks;
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
