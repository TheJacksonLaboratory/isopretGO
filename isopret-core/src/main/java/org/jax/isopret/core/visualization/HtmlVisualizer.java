package org.jax.isopret.core.visualization;

import org.jax.isopret.core.interpro.DisplayInterproAnnotation;
import org.jax.isopret.core.interpro.InterproEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * This class generates HTML code to visualize individual genes.
 * It generates two main kinds of files. The first is to export one page with all
 * information on a single gene ({@link #getHtml(Visualizable)}. The other
 * generates a slightly shorter version of this and is intended for the page
 * that contains all genes annotated to some GO term ({@link #getShortHtml(Visualizable)}.
 * @author Peter Robinson
 */
public class HtmlVisualizer implements Visualizer {
    private static final Logger LOGGER = LoggerFactory.getLogger(HtmlVisualizer.class);

    public final String experiment;

    public final String HBADEALS_A = """
        <a href="https://genomebiology.biomedcentral.com/articles/10.1186/s13059-020-02072-6"
         target=__blank>HBA-DEALS</a>
         """;

    public HtmlVisualizer(String experiment) {
        this.experiment = experiment;
    }

      /*
      Create the HTML which will go here in the FreeMarker template
      <article>
        ${gene}
      </article>
     */

    @Override
    public String getHtml(Visualizable vis) {
        return  "<h1>" + vis.getGeneSymbol() + "</h1>\n" +
                getSingleGeneSummary(vis) +
                getSingleGeneTranscriptSummary(vis) +
                getTranscriptBox(vis) +
                "<div class=\"svgrow\">\n" +
                vis.getIsoformSvg() +
                "</div>\n" +
                getProteinDomainSummary(vis) +
               getInterproBox(vis) + "\n" +
                "<div class=\"svgrow\">\n" +
                vis.getProteinSvg() +
                "</div>\n" +
                getGoSummary(vis) +
                "<div class=\"svgrow\">\n" +
                vis.getGoHtml() +
             "</div>\n"
                ;
    }


    protected String wrapInArticle(String html, String geneSym) {
        return "<a name=\"" + geneSym + "\"></a>" +
                "<article>" +
                "<h2>" + geneSym + "</h2>" +
                html +
                "</article>\n";
    }


    @Override
    public String getShortHtml(Visualizable visualizable) {
        String html = getShortSingleGeneSummary(visualizable) +
                getTranscriptBox(visualizable) +
                "<div class=\"svgrow\">\n" +
                visualizable.getIsoformSvg() +
                "</div>\n" +
                getProteinDomainSummary(visualizable) +
                getInterproBox(visualizable) + "\n" +
                "<div class=\"svgrow\">\n" +
                visualizable.getProteinSvg() +
                "</div>\n</div>\n";
        return wrapInArticle(html, visualizable.getGeneSymbol());
    }

    /**
     * Show a short summary of the gene and its transcripts.
     * @param visualizable {@link Visualizable} object representing a gene
     * @return HTML code with a summary of the gene
     */
    private String getShortSingleGeneSummary(Visualizable visualizable) {
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


    public String getSingleGeneSummary(Visualizable visualizable) {
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

        return "<section>" +
                "<a name=\"geneSummary\"></a>\n" +
                "<article>" +
                "<h2>Isopret summary for " + visualizable.getGeneSymbol() + "</h2>\n" +
                "<p>" + symbol + " (" + esemblAnchor + ") had an expression fold change of " +
                String.format("%.3f", expressionFC) + ", corresponding to a log<sub>2</sub> fold change of " +
                String.format("%.3f", expressionLogFc) + ". The posterior error probability (PEP) according the the " + HBADEALS_A +
                " analysis was " + String.format("%e", expressionP) + ".</p>" +
                "<p>The gene has a total of " +
                totalTranscriptCount + " annotated transcripts in Ensembl, of which " +
                expressedTranscriptCount + " were expressed in the current experiment (" + experiment + "). " +
                "Of these, " + signDiffIsoCount + " were found to be differentially spliced." +
                "</p>\n" +
                "</article>\n" +
                "</section>\n";
    }

    public String getSingleGeneTranscriptSummary(Visualizable visualizable) {
        String symbol = visualizable.getGeneSymbol();
        int totalTranscriptCount = visualizable.getTotalTranscriptCount();
        int expressedTranscriptCount = visualizable.getExpressedTranscriptCount();
        int signDiffIsoCount = visualizable.getDifferentialTranscriptCount();
        return "<section>" +
                "<a name=\"isoSummary\"></a>\n" +
                "<article>" +
                "<H3>Isoform analysis</H3>\n" +
                "<p>" + symbol + " has a total of " +
                totalTranscriptCount + " transcripts annotated in Ensembl, of which " +
                expressedTranscriptCount + " were expressed in the current experiment (" + experiment + ")." +
                "Of these, " + signDiffIsoCount +
                (signDiffIsoCount == 1 ? " was":"were") +
                " found to be differentially spliced." +
                "</p>\n" +
                "</article>\n" +
                "</section>\n";
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


    private final static String INTERPRO_TABLE_HEADER = """
            <table>
              <thead>
                <tr>
                  <th>Interpro id</th>
                  <th>Name</th>
                  <th>Type</th>
                </tr>
              </thead>
            """;

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
            sb.append("<tr><td>").append(interproAnchor).append("</td>");
            sb.append("<td>").append(entry.getDescription()).append("</td>\n");
            sb.append("<td>").append(entry.getEntryType()).append("</td></tr>\n");
        }
        sb.append("</table>\n");
        return sb.toString();
    }


    private String getGoAnchor(OntologyTermVisualizable go) {
        //QuickGO - https://www.ebi.ac.uk/QuickGO/term/GO:0006915
        String url = "https://www.ebi.ac.uk/QuickGO/term/" + go.getTermId();
        return String.format("<a href=\"%s\" target=\"_blank\">%s</a>\n", url, go.getTermLabel());
    }






    private final static String ISOFORM_TABLE_HEADER = """
            <table>
              <thead>
                <tr>
                  <th>Isoform</th>
                  <th>Log<sub>2</sub> fold change</th>
                  <th>Probability (PEP)</th>
                </tr>
              </thead>
            """;

    private String getTranscriptBox(Visualizable vis) {
        StringBuilder sb = new StringBuilder();
        List<IsoformVisualizable> tableData = vis.getIsoformVisualizable();
        if (tableData.isEmpty()) {
            return "<p>No isoform data found.</p>\n";
        }
        sb.append(ISOFORM_TABLE_HEADER);
        for (var row : tableData) {
            sb.append("<tr><td>").append(row.transcriptAccession()).append("</td>");
            sb.append("<td>").append(row.log2Foldchange()).append("</td>");
            sb.append("<td>").append(row.isoformP()).append("</td></tr>\n");
        }
        sb.append("</table>\n");
        return sb.toString();
    }


    private String getGoSummary(Visualizable vis) {

        return   "<section>" +
                "<a name=\"goSummary\"></a>\n" +
                "<article>" +
                "<H3>Gene Ontology analysis</H3>\n" +
                "<p>" + vis.getGeneSymbol() + " has a total of " +
                vis.getAnnotationGoIds().size() + " Gene Ontology Annotations. The following" +
                " table show the predictions of our algorithm as to how the annotations" +
                " are distributed across the isoforms.</p>" +
                "</article>\n" +
                "</section>\n";
    }






}
