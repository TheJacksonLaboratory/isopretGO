package org.jax.isopret.gui.service;

import org.jax.isopret.core.visualization.HtmlUtil;
import org.jax.isopret.core.visualization.Visualizable;
import org.monarchinitiative.phenol.ontology.data.TermId;


public class GoSingleGeneSplicingVisualizer extends AnnotatedGenesVisualizer {

    private final int nGenesWithDifferentialSplicing;
    private final int nDifferentialIsoforms;

    public GoSingleGeneSplicingVisualizer(TermId goId, IsopretService isopretService) {
        super(goId, isopretService);
        this.visualizableList = isopretService.getDasForGoTerm(goId);
        nGenesWithDifferentialSplicing = visualizableList.size();
        nDifferentialIsoforms = visualizableList.stream()
                .map(Visualizable::getDifferentialTranscriptCount)
                .reduce(0, Integer::sum);
    }

    public String getTitle() {
        return String.format("Isopret: %d differentially spliced genes annotated to %s (%s)",
                nGenesWithDifferentialSplicing,  this.termLabel, this.geneOntologyId.getValue());
    }

    public String export() {
        StringBuilder sb = new StringBuilder();
        sb.append(htmlHeader);
        sb.append(htmlTop());
        sb.append(getGeneULwithLinks());
        for (var viz : visualizableList) {
            String html = getHtml(viz);
            sb.append(wrapInArticle(html, viz.getGeneSymbol()));
        }
        sb.append(bottom);
        return sb.toString();
    }

    private String getGeneULwithLinks() {
        StringBuilder sb = new StringBuilder();
        sb.append("<p>A total of ").append(visualizableList.size()).append(" genes that are annotated to ");
        sb.append(this.termLabel).append(" (").append(geneOntologyId.getValue()).append(") were ");
        sb.append("identified as differentially spliced. ");
        sb.append("A total of ").append(nDifferentialIsoforms).append(" differentially spliced isoforms were associated with these genes.");
        sb.append("</p>");
        sb.append("<ul>\n");
        for (var viz : visualizableList) {
            sb.append("<li><a href=\"#").append(viz.getGeneSymbol()).append("\">").append(viz.getGeneSymbol()).append("</a></li>");
        }
        sb.append("</ul>");
        sb.append("<br/><br/>");
        return sb.toString();
    }

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
                this.termLabel, this.geneOntologyId, HtmlUtil.css);
    }
}
