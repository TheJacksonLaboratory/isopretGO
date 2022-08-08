package org.jax.isopret.gui.service;

import org.jax.isopret.visualization.HtmlUtil;
import org.monarchinitiative.phenol.ontology.data.TermId;

public class GoSingleGeneExpressionVisualizer extends AnnotatedGenesVisualizer {



    public GoSingleGeneExpressionVisualizer(TermId goId, IsopretService isopretService) {
        super(goId, isopretService);
        this.visualizableList = isopretService.getDgeForGoTerm(goId);
    }


    public String getTitle() {
        return String.format("Isopret: %d differentially expressed genes annotated to %s (%s)",
                this.visualizableList.size(),  this.termLabel, this.geneOntologyId.getValue());
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
        sb.append("identifed as differentially expressed.");
        sb.append("</p>");
        sb.append("<ul>\n");
        for (var viz : visualizableList) {
            sb.append("<li>").append(viz.getGeneSymbol()).append("</li>");
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
                <title>Isopret: Differentially expressedgenes annotated to %s (%s)</title>
                <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
                %s
                </head>
                """, this.termLabel, this.geneOntologyId, HtmlUtil.css);
    }

}
