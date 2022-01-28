package org.jax.isopret.gui.service;

import org.jax.isopret.core.visualization.HtmlUtil;
import org.jax.isopret.core.visualization.Visualizable;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;

import java.io.File;
import java.util.List;
import java.util.Optional;

public class GoHtmlExporter {

    enum Mode {SPLICING, EXPRESSION};

    private final Mode analysisMode;

    private final String basename;

    private final TermId geneOntologyId;
    private final String geneOntologyLabel;
    private final String htmlHeader;
    private final List<Visualizable> annotatedGenes;

    public GoHtmlExporter(TermId goId, IsopretService isopretService, Mode mode) {
        this.geneOntologyId = goId;
        Ontology ontology = isopretService.getGeneOntology();
        this.analysisMode = mode;
        this.geneOntologyLabel = ontology.getTermLabel(goId).orElse("n/a");
        // need to get all visualizables for the current go term
        if (mode.equals(Mode.SPLICING)) {
            this.annotatedGenes = isopretService.getDasForGoTerm(goId);
        } else {
            this.annotatedGenes = isopretService.getDgeForGoTerm(goId);
        }
        String title = getTitle();
        this.htmlHeader = header();
        Optional<File> opt = isopretService.getHbaDealsFileOpt();
        basename = opt.map(File::getName).orElse("n/a");
    }

    private String getTitle() {
        int n = this.annotatedGenes.size();
        String go = String.format("%s (%s)", this.geneOntologyLabel, this.geneOntologyId.getValue());
        return String.format("Isopret: %d differentially %s genes annotated to %s",
               n,  this.analysisMode.equals(Mode.SPLICING) ? "spliced" : "expressed", go);
    }

    public String export() {
        StringBuilder sb = new StringBuilder();
        sb.append(htmlHeader);
        sb.append(htmlTop());
        sb.append( getGeneULwithLinks() );
        final GoSummaryHtmlVisualizer visualizer = new GoSummaryHtmlVisualizer(basename);
        for (var viz : annotatedGenes) {
            String html = visualizer.getHtml(viz);
            html = HtmlUtil.wrap(html);
            sb.append(html);
        }
        sb.append(bottom);
        return sb.toString();
    }

    private String getGeneULwithLinks() {
        StringBuilder sb = new StringBuilder();
        sb.append("<p>A total of ").append(annotatedGenes.size()).append(" genes that are annotated to ");
        sb.append(this.geneOntologyLabel).append(" (").append(geneOntologyId.getValue()).append(") were ");
        sb.append("identifed as differentially ").append(this.analysisMode.equals(Mode.SPLICING) ? "spliced.":"expressed.");
        sb.append("</p>");
        sb.append("<ul>\n");
        for (var viz : annotatedGenes) {
            sb.append("<li>").append(viz.getGeneSymbol()).append("</li>");
        }
        sb.append("</ul>");
        sb.append("<br/><br/>");
        return sb.toString();
    }



    public static final String bottom = """
           <span id="tooltip" display="none" style="position: absolute; display: none;"></span>
           </main>
           <footer>
               <p>Isopret &copy; 2022</p>
           </footer>
            </body>
            </html>
            """;


    public static GoHtmlExporter splicing(TermId goId, IsopretService isopretService) {
        return new GoHtmlExporter(goId, isopretService, Mode.SPLICING);
    }

    public static GoHtmlExporter expression(TermId goId, IsopretService isopretService) {
        return new GoHtmlExporter(goId, isopretService, Mode.EXPRESSION);
    }


    private String header() {
        return String.format(
                """
                <!doctype html>
                <html class="no-js" lang="">
                <head>
                <meta charset="utf-8">
                <meta http-equiv="x-ua-compatible" content="ie=edge">
                <title>Isopret: Differentially %s genes annotated to %s (%s)</title>
                <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
                %s
                </head>
                """, this.analysisMode.equals(Mode.SPLICING) ? "spliced" : "expressed",
                this.geneOntologyLabel, this.geneOntologyId, HtmlUtil.css);
    }

    private  String htmlTop() {
        return String.format("""
            <body>
              <!--[if lte IE 9]>
                <p class="browserupgrade">You are using an <strong>outdated</strong> browser. Please <a href="https://browsehappy.com/">upgrade your browser</a> to improve your experience and security.</p>
              <![endif]-->
            <header class="banner">
                <h1><font color="#FFDA1A">%s</font></h1>
            </header>
            <main>
            """, getTitle());
    }

}
