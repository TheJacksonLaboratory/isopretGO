package org.jax.isopret.gui.service;

import org.jax.isopret.core.analysis.InterproOverrepResult;
import org.jax.isopret.core.impl.go.GoTermIdPlusLabel;
import org.jax.isopret.core.impl.hbadeals.HbaDealsTranscriptResult;
import org.jax.isopret.model.DisplayInterproAnnotation;
import org.jax.isopret.model.AccessionNumber;
import org.jax.isopret.model.AnnotatedGene;
import org.jax.isopret.visualization.HtmlUtil;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Display HTML file with genes and isoforms annotated to a given Interpro entry.
 * We show all genes with one or more isoforms that (1) are annotated to the interpro entry in question and (2) are
 * significantly differentially spliced.
 *
 * @author Peter Robinson
 */
public class InterproAnnotatedGenesVisualizer extends AnnotatedGenesVisualizer {
    private static final Logger LOGGER = LoggerFactory.getLogger(InterproAnnotatedGenesVisualizer.class);
    private final InterproOverrepResult interproResult;
    /**
     * Genes we will include in the HTML display.
     */
    private final List<AnnotatedGene> includedGenes;
    /**
     * GO annotations for differential isoforms.
     */
    private final Map<GoTermIdPlusLabel, Integer> countsMap;

    public InterproAnnotatedGenesVisualizer(InterproOverrepResult interpro, IsopretService isopretService) {
        super(interpro, isopretService);
        this.interproResult = interpro;
        int targetInterproId = this.interproResult.interproEntry().getId();
        double splicingPepThreshold = isopretService.getSplicingPepThreshold();
        List<AnnotatedGene> annotatedGeneList = isopretService.getAnnotatedGeneList();
        this.includedGenes = new ArrayList<>();
        Set<TermId> transcriptAccessions = new HashSet<>();
        for (AnnotatedGene gene : annotatedGeneList) {
            Map<AccessionNumber, List<DisplayInterproAnnotation>> transcriptMap = gene.getTranscriptToInterproHitMap();
            boolean includeThisGene = false;
            if (gene.passesSplicingThreshold()) {
                for (HbaDealsTranscriptResult tresult : gene.getHbaDealsResult().getTranscriptResults()) {
                    if (tresult.isSignificant(splicingPepThreshold)) {
                        AccessionNumber acc = tresult.getTranscriptId();
                        List<DisplayInterproAnnotation> dialist = transcriptMap.get(acc);
                        if (dialist == null) {
                            // no annotations for this transcript, which is expected for some
                            continue;
                        }
                        for (var dia : dialist) {
                            if (dia.getInterproEntry().getId() == targetInterproId) {
                                includeThisGene = true;
                                transcriptAccessions.add(acc.toTermId());
                            }
                        }
                    }
                }
            }
            if (includeThisGene) {
                LOGGER.info("Including gene {} for interpro export", gene.getSymbol());
                includedGenes.add(gene);
            }
        }
        // when we get here, we want to display only the genes in "incudedGenes".
        // let's get a Table with their GO annotations.
        LOGGER.info("Total of {} included genes", includedGenes.size());
        LOGGER.info("Gettings transcripts for interpro output. n={}", transcriptAccessions.size());
        this.countsMap = isopretService.getGoAnnotationsForTranscript(transcriptAccessions);
        Set<AccessionNumber> includedEnsgSet = includedGenes
                .stream()
                .map(AnnotatedGene::getGeneAccessionNumber)
                .collect(Collectors.toSet());
        this.visualizableList = isopretService.getGeneVisualizables(includedEnsgSet);
    }


    public String export() {
        StringBuilder sb = new StringBuilder();
        sb.append(htmlHeader);
        sb.append(htmlTop());
        sb.append(getGoTable());
        sb.append(getGeneULwithLinks());
        for (var viz : visualizableList) {
            String html = getHtml(viz);
            sb.append(wrapInArticle(html, viz.getGeneSymbol()));
        }
        sb.append(bottom);
        return sb.toString();
    }


    private String getGoTable() {
        StringBuilder sb = new StringBuilder();

        //Map<GoTermIdPlusLabel, Integer> countsMap
        List<Map.Entry<GoTermIdPlusLabel, Integer>> list = new ArrayList<>(countsMap.entrySet());
        int THRESHOLD = 3;
        int aboveThreshold = (int) list.stream().filter(e -> e.getValue() >= THRESHOLD).count();
        list.sort(Map.Entry.<GoTermIdPlusLabel, Integer>comparingByValue().reversed());
        sb.append("<p>Total of ").append(list.size()).append(" GO terms annotated to the transcripts. Of these, ");
        sb.append(aboveThreshold).append(" were above the threshold of ").append(THRESHOLD).append(" annotations")
                .append(" and are shown here.</p>");
        sb.append(htmlTableHeader());
        for (var entry : list) {
            if (entry.getValue() >= THRESHOLD)
                sb.append(getRow(entry.getKey(), entry.getValue()));
        }
        sb.append("</table>\n");
        return sb.toString();
    }

    private String getRow(GoTermIdPlusLabel goTermIdPlusLabel, Integer count) {
        return "<tr><td>" + goTermIdPlusLabel.getLabel() + "</td>" +
                "<td>" + goTermIdPlusLabel.getId() + "</td>" +
                "<td>" + count + "</td></tr>\n";
    }

    private String htmlTableHeader() {
        return "<table class=\"go\">" +
                "<tr>" +
                "<th width=\"400px\";>GO term</th><th>id</th>\"" +
                "<th>Annotated transcripts</th>" +
                "</tr>";
    }

    private final static String HTML_HEADER = """
            <!doctype html>
            <html class="no-js" lang="">

            <head>
              <meta charset="utf-8">
              <meta http-equiv="x-ua-compatible" content="ie=edge">
               <style>
            html, body {
               padding: 0;
               margin: 20;
               font-size:14px;
            }

            body {
               font-family:"DIN Next", Helvetica, Arial, sans-serif;
               line-height:1.25;
               background-color:white   ;
                max-width:1200px;
                margin-left:auto;
                margin-right:auto;
             }
             gotable.th
             {
               vertical-align: bottom;
               text-align: center;
             }
             
             gotable.th span
             {
               -ms-writing-mode: tb-rl;
               -webkit-writing-mode: vertical-rl;
               writing-mode: vertical-rl;
               transform: rotate(180deg);
               white-space: nowrap;
               padding: 5px 10px;
                margin: 0 auto;
             }
            </style>
            <body>
            """;

    private static final String HTML_FOOTER = """
            </body>
            </html>
            """;


    String header() {
        return String.format(
                """
                        <!doctype html>
                        <html class="no-js" lang="">
                        <head>
                        <meta charset="utf-8">
                        <meta http-equiv="x-ua-compatible" content="ie=edge">
                        <title>Isopret: Differentially spliced genes annotated to %s (%s)</title>
                        <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
                        %s
                        </head>
                        """,
                this.termLabel, this.interproId, HtmlUtil.css);
    }

    protected static final String bottom = """
            <span id="tooltip" display="none" style="position: absolute; display: none;"></span>
            </main>
            <footer>
                <p>Isopret &copy; 2022</p>
            </footer>
             </body>
             </html>
             """;


    public String getTitle() {
        return String.format("Isopret: %d differentially spliced genes annotated to %s (%s)",
                includedGenes.size(), this.termLabel, this.interproId);
    }


    private String getGeneULwithLinks() {
        StringBuilder sb = new StringBuilder();
        sb.append("<p>A total of ").append(includedGenes.size()).append(" genes that are annotated to ");
        sb.append(this.termLabel).append(" (").append(interproId).append(") were ");
        sb.append("identified as differentially spliced. ");
        sb.append("</p>");
        for (var viz : visualizableList) {
            sb.append("<li><a href=\"#").append(viz.getGeneSymbol()).append("\">").append(viz.getGeneSymbol()).append("</a></li>");
        }
        sb.append("<br/><br/>");
        return sb.toString();
    }


}
