package org.jax.isopret.gui.service;

import org.jax.isopret.core.analysis.InterproOverrepResult;
import org.jax.isopret.core.interpro.DisplayInterproAnnotation;
import org.jax.isopret.core.interpro.InterproEntry;
import org.jax.isopret.core.visualization.IsoformVisualizable;
import org.jax.isopret.core.visualization.Visualizable;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;

import java.io.File;
import java.util.List;
import java.util.Optional;

public abstract class AnnotatedGenesVisualizer {

    private final String HBADEALS_A = """
        <a href="https://genomebiology.biomedcentral.com/articles/10.1186/s13059-020-02072-6"
         target=__blank>HBA-DEALS</a>
         """;

    protected final String htmlHeader;
    protected final String basename;
    protected final TermId geneOntologyId;
    protected final String interproId;
    protected final String termLabel;
    protected  List<Visualizable> annotatedGenes;
    public final String experiment;

    public AnnotatedGenesVisualizer(TermId goId, IsopretService isopretService) {
        this.geneOntologyId = goId;
        Ontology ontology = isopretService.getGeneOntology();
        this.termLabel = ontology.getTermLabel(goId).orElse("n/a");
        interproId = null;
        Optional<File> opt = isopretService.getHbaDealsFileOpt();
        basename = opt.map(File::getName).orElse("n/a");
        this.htmlHeader = header();
        Optional<File> optHba =  isopretService.getHbaDealsFileOpt();
        this.experiment = optHba.map(File::getName).orElse("n/a");
    }

    public AnnotatedGenesVisualizer(InterproOverrepResult interpro, IsopretService isopretService) {
        this.geneOntologyId = null;
        this.interproId = interpro.interproAccession();
        this.termLabel = interpro.interproDescription();
        Optional<File> opt = isopretService.getHbaDealsFileOpt();
        basename = opt.map(File::getName).orElse("n/a");
        this.htmlHeader = header();
        Optional<File> optHba =  isopretService.getHbaDealsFileOpt();
        this.experiment = optHba.map(File::getName).orElse("n/a");
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


    public static AnnotatedGenesVisualizer splicing(TermId goId, IsopretService isopretService) {
        return new GoSingleGeneSplicingVisualizer(goId, isopretService);
    }

    public static AnnotatedGenesVisualizer expression(TermId goId, IsopretService isopretService) {
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




    protected String getSingleGeneSummary(Visualizable visualizable) {
        String symbol = visualizable.getGeneSymbol();
        String ensemblGeneAccession = visualizable.getGeneAccession();
        int totalTranscriptCount = visualizable.getTotalTranscriptCount();
        int expressedTranscriptCount = visualizable.getExpressedTranscriptCount();
        String ensemblUrl = visualizable.getGeneEnsemblUrl();
        String esemblAnchor = String.format("<a href=\"%s\" target=\"__blank\">%s</a>\n", ensemblUrl, ensemblGeneAccession);
        double expressionFC = visualizable.getExpressionFoldChange();
        double expressionLogFc = visualizable.getExpressionLogFoldChange();
        double expressionP = visualizable.getExpressionPep();
        int signDiffIsoCount = visualizable.getDifferentialTranscriptCount();

        return  "<p>" + symbol + " (" + esemblAnchor + ") had an expression fold change of " +
                String.format("%.3f", expressionFC) + ", corresponding to a log<sub>2</sub> fold change of " +
                String.format("%.3f", expressionLogFc) + ". The posterior error probability (PEP) according the the " + HBADEALS_A +
                " analysis was " + String.format("%e", expressionP) + ".</p>" +
                "<p>The gene has a total of " +
                totalTranscriptCount + " annotated transcripts in Ensembl, of which " +
                expressedTranscriptCount + " were expressed in the current experiment (" + experiment + "). " +
                "Of these, " + signDiffIsoCount + " were found to be differentially spliced." +
                "</p>\n" +
                "<p>" + symbol + " has a total of " +
                totalTranscriptCount + " transcripts annotated in Ensembl, of which " +
                expressedTranscriptCount + " were expressed in the current experiment (" + experiment + ")." +
                "Of these, " + signDiffIsoCount +
                (signDiffIsoCount == 1 ? " was":"were") +
                " found to be differentially spliced." +
                "</p>\n";
    }

    public String getHtml(Visualizable vis) {
        return "<div class=\"go\">\n" +
                getSingleGeneSummary(vis) +
                getTranscriptBox(vis) +
                "<div class=\"svgrow\">\n" +
                vis.getIsoformSvg() +
                "</div>\n" +
                getProteinDomainSummary(vis) +
                getInterproBox(vis) + "\n" +
                "<div class=\"svgrow\">\n" +
                vis.getProteinSvg() +
                "</div>\n</div>\n";
    }





    private final static String INTERPRO_TABLE_HEADER = """
            <table>
              <thead>
                <tr>
                  <th class="go">Interpro id</th>
                  <th class="go">Name</th>
                  <th class="go">Type</th>
                </tr>
              </thead>
            """;

    private String getTranscriptBox(Visualizable vis) {
        StringBuilder sb = new StringBuilder();
        List<IsoformVisualizable> tableData = vis.getIsoformVisualizable();
        if (tableData.isEmpty()) {
            return "<p>No isoform data found.</p>\n";
        }
        sb.append(INTERPRO_TABLE_HEADER);
        for (var row : tableData) {
            sb.append("<tr><td class=\"go\">").append(row.transcriptAccession()).append("</td>");
            sb.append("<td class=\"go\">").append(row.log2Foldchange()).append("</td>");
            sb.append("<td class=\"go\">").append(row.isoformP()).append("</td></tr>\n");
        }
        sb.append("</table>\n");
        return sb.toString();
    }

    public String getProteinDomainSummary(Visualizable visualizable) {
        String symbol = visualizable.getGeneSymbol();
        int totalInterproDomains = visualizable.getInterproForExpressedTranscripts().size();
        int codingTranscriptCount = visualizable.getCodingTranscriptCount();
        int totalTranscriptCount = visualizable.getTotalTranscriptCount();

        if (codingTranscriptCount == 0) {
            return   "<section>" +
                    "<a name=\"domainSummary\"></a>\n" +
                    "<article>" +
                    "<H3>Protein domain analysis</H3>\n" +
                    "<p>" + symbol + " has a total of " +
                    totalInterproDomains + " transcripts annotated in Ensembl, none of which " +
                    " are coding.</p>" +
                    "</article>\n" +
                    "</section>\n";
        }

        return   "<section>" +
                "<a name=\"domainSummary\"></a>\n" +
                "<article>" +
                "<H3>Protein domain analysis</H3>\n" +
                "<p>" + symbol + " has a total of " +
                totalTranscriptCount + " transcripts annotated in Ensembl, of which " +
                codingTranscriptCount +
                (codingTranscriptCount == 1? " is":" are") +
                " coding." +
                "Isoprot lists " + totalInterproDomains +
                " domains for the coding isoforms." +
                "</p>\n" +
                "</article>\n" +
                "</section>\n";
    }

    public String getInterproBox(Visualizable vis) {
        StringBuilder sb = new StringBuilder();
        List<DisplayInterproAnnotation> introProAnnotations = vis.getInterproForExpressedTranscripts();
        List<InterproEntry> entryList = introProAnnotations.stream()
                .map(DisplayInterproAnnotation::getInterproEntry)
                .distinct()
                .sorted()
                .toList();
        if (introProAnnotations.isEmpty()) {
            return "<p><i>No interpro domains found.</i></p>\n";
        }
        sb.append(INTERPRO_TABLE_HEADER);
        for (InterproEntry entry : entryList) {
            String interproUrl = String.format("https://www.ebi.ac.uk/interpro/entry/InterPro/%s/", entry.getIntroproAccession());
            String interproAnchor = String.format("<a href=\"%s\" target=\"_blank\">%s</a>", interproUrl, entry.getIntroproAccession());
            sb.append("<tr><td class=\"go\">").append(interproAnchor).append("</td>");
            sb.append("<td class=\"go\">").append(entry.getDescription()).append("</td>\n");
            sb.append("<td class=\"go\">").append(entry.getEntryType()).append("</td></tr>\n");
        }
        sb.append("</table>\n");
        return sb.toString();
    }
}
