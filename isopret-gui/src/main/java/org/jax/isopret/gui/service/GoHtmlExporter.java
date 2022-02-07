package org.jax.isopret.gui.service;

import org.jax.isopret.core.visualization.Visualizable;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;

import java.io.File;
import java.util.List;
import java.util.Optional;

public abstract class GoHtmlExporter {

    protected final String htmlHeader;
    protected final String basename;
    protected final TermId geneOntologyId;
    protected final String geneOntologyLabel;
    protected  List<Visualizable> annotatedGenes;


    public GoHtmlExporter(TermId goId, IsopretService isopretService) {
        this.geneOntologyId = goId;
        Ontology ontology = isopretService.getGeneOntology();
        this.geneOntologyLabel = ontology.getTermLabel(goId).orElse("n/a");
        Optional<File> opt = isopretService.getHbaDealsFileOpt();
        basename = opt.map(File::getName).orElse("n/a");
        this.htmlHeader = header();
    }

    abstract public String getTitle();
    abstract public String export();
    abstract String header();


    protected static final String bottom = """
           <span id="tooltip" display="none" style="position: absolute; display: none;"></span>
           </main>
           <footer>
               <p>Isopret &copy; 2022</p>
           </footer>
            </body>
            </html>
            """;


    public static GoHtmlExporter splicing(TermId goId, IsopretService isopretService) {
        return new GoSingleGeneSplicingVisualizer(goId, isopretService);
    }

    public static GoHtmlExporter expression(TermId goId, IsopretService isopretService) {
        return new GoSingleGeneExpressionVisualizer(goId, isopretService);
    }




    protected String htmlTop() {
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


    protected String wrapInArticle(String html, String geneSym) {
       return "<a name=\"" + geneSym + "\"></a>" +
                "<article>" +
                "<h2>" + geneSym + "</h2>" +
                html +
                "</article>\n";
    }
}
