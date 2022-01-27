package org.jax.isopret.gui.service;

import org.jax.isopret.core.visualization.HtmlUtil;
import org.jax.isopret.core.visualization.HtmlVisualizer;
import org.jax.isopret.core.visualization.Visualizable;
import org.monarchinitiative.phenol.ontology.algo.OntologyAlgorithm;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class GoHtmlExporter {

    enum Mode {SPLICING, EXPRESSION};

    private final String basename;

    private final TermId geneOntologyId;
    private final String geneOntologyLabel;
    private final String htmlHeader;
    private final List<Visualizable> annotatedGenes;

    public GoHtmlExporter(TermId goId, IsopretService isopretService, Mode mode) {
        this.geneOntologyId = goId;
        Ontology ontology = isopretService.getGeneOntology();
        this.geneOntologyLabel = ontology.getTermLabel(goId).orElse("n/a");
        this.htmlHeader = header(goId.getValue(), geneOntologyLabel, mode);
        Optional<File> opt = isopretService.getHbaDealsFileOpt();
        basename = opt.map(File::getName).orElse("n/a");
        // need to get all visualizables for the current go term
        if (mode.equals(Mode.SPLICING)) {
            this.annotatedGenes = isopretService.getDasForGoTerm(goId);
        } else {
            this.annotatedGenes = isopretService.getDgeForGoTerm(goId);
        }
    }

    public String export() {
        StringBuilder sb = new StringBuilder();
        sb.append(htmlHeader);
        final HtmlVisualizer visualizer = new HtmlVisualizer(basename);
        for (var viz : annotatedGenes) {
            String html = visualizer.getHtml(viz);
            html = HtmlUtil.wrap(html);
            sb.append(html);
        }
        sb.append(bottom);
        return sb.toString();
    }

    public static final String bottom = """
           <span id="tooltip" display="none" style="position: absolute; display: none;"></span>
           </main>
           <footer>
               <p>Isopret &copy; 2022</p>
           </footer>
            <script>
              function showTooltip(evt, text) {
                let tooltip = document.getElementById("tooltip");
                tooltip.innerText = text;
                tooltip.style.display = "block";
                tooltip.style.left = evt.pageX + 10 + 'px';
                tooltip.style.top = evt.pageY + 10 + 'px';
              }
                        
              function hideTooltip() {
                var tooltip = document.getElementById("tooltip");
                tooltip.style.display = "none";
              }
            </script>
            </body>
            </html>
            """;


    public static GoHtmlExporter splicing(TermId goId, IsopretService isopretService) {
        return new GoHtmlExporter(goId, isopretService, Mode.SPLICING);
    }

    public static GoHtmlExporter expression(TermId goId, IsopretService isopretService) {
        return new GoHtmlExporter(goId, isopretService, Mode.EXPRESSION);
    }


    public static String header(String goId, String goLabel, Mode mode) {
        return String.format(
                """
                        <!doctype html>
                        <html class="no-js" lang="">
                        <head>
                          <meta charset="utf-8">
                          <meta http-equiv="x-ua-compatible" content="ie=edge">
                          <title>Isopret: Differentially %s genes annotated to %s (%s)</title>
                          <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
                        </head>
                        """, mode.equals(Mode.SPLICING) ? "spliced" : "expressed", goLabel, goId);
    }
}
