package org.jax.isopret.core.visualization;

import org.jax.isopret.core.interpro.DisplayInterproAnnotation;
import org.jax.isopret.core.interpro.InterproEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class HtmlVisualizer implements Visualizer {
    private static final Logger LOGGER = LoggerFactory.getLogger(HtmlVisualizer.class);
    public HtmlVisualizer() {
    }


    private final static String GENE_TABLE_HEADER = """
            <table>
              <thead>
                <tr>
                  <th>Gene</th>
                  <th>Chrom.</th>
                  <th>Log<sub>2</sub> fold change</th>
                  <th>Probability (PEP)</th>
                </tr>
              </thead>
            """;


    private String getGoAnchor(OntologyTermVisualizable go) {
        //QuickGO - https://www.ebi.ac.uk/QuickGO/term/GO:0006915
        String url = "https://www.ebi.ac.uk/QuickGO/term/" + go.getTermId();
        return String.format("<a href=\"%s\" target=\"_blank\">%s</a>\n", url, go.getTermLabel());
    }

    public String getGeneBox(Visualizable vis) {
        StringBuilder sb = new StringBuilder();
        sb.append(GENE_TABLE_HEADER);
        String a = String.format("<a href=\"%s\" target=\"__blank\">%s</a>\n", vis.getGeneUrl(), vis.getGeneSymbol());
        sb.append("<tr><td>").append(a).append("</td>");
        sb.append("<td>").append(vis.getChromosome()).append("</td>\n");
        sb.append(String.format("<td>%.2f</td>", vis.getExpressionLogFoldChange()));
        String prob = String.format("%.2f", vis.getExpressionPval()) + (vis.isDifferentiallyExpressed() ? " (*)" : "");
        sb.append("<td>").append(prob).append("</td></tr>\n");
        sb.append("</table>\n");
        List<OntologyTermVisualizable> goterms = vis.getGoTerms();
        if (goterms.size() > 0) {
            sb.append("<p>Enriched GO terms associated with ").append(vis.getGeneSymbol()).append(".</p>\n");
            sb.append("<ul>\n");
            for (var go : goterms) {
                sb.append("<li>").append(getGoAnchor(go)).append("</li>\n");
            }
            sb.append("</ul>\n");
        }
        return sb.toString();
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
        final int EXPECTED_N_COLUMNS = 3;
        StringBuilder sb = new StringBuilder();
        List<IsoformVisualizable> tableData = vis.getIsoformVisualizable();
        if (tableData.isEmpty()) {
            return "<p>No isoform data found.</p>\n";
        }
        int totalIsoforms = vis.getTotalTranscriptCount();
        int expressionIsoforms = vis.getExpressedTranscriptCount();
        sb.append(ISOFORM_TABLE_HEADER);
        for (var row : tableData) {

            sb.append("<tr><td>").append(row.transcriptAccession()).append("</td>");
            sb.append("<td>").append(row.log2Foldchange()).append("</td>");
            sb.append("<td>").append(row.isoformP()).append("</td></tr>\n");
        }
        sb.append("</table>\n");
        sb.append("<p>").append(expressionIsoforms).append(" transcripts were expressed in the data from ")
                .append(totalIsoforms).append(" annotated transcripts.</p>\n");
        return sb.toString();
    }


    public String getGeneNameAndBadges(Visualizable vis) {
        StringBuilder sb = new StringBuilder();
        int i = vis.getI();
        sb.append("<h1>").append(i).append(") ").append(vis.getGeneSymbol());
        if (vis.isDifferentiallyExpressed()) {
            sb.append(" <span class=\"badge\">DGE</span> ");
        }
        if (vis.isDifferentiallySpliced()) {
            sb.append(" <span class=\"badge\">DAS</span>");
        }
        sb.append("</h1>\n");
        return sb.toString();
    }


    /*
      Create the HTML which will go here in the FreeMarker template
      <article>
        ${gene}
      </article>
     */

    @Override
    public String getHtml(Visualizable vis) {
        return getGeneNameAndBadges(vis) +
                "<article>\n" +
                "<div class=\"generow\">\n" +
                "<div class=\"column\" style=\"background-color:#F8F8F8;\">\n" +
                getGeneBox(vis) + "\n" +
                "</div>\n" +
                "<div class=\"column\" style=\"background-color:#F0F0F0;\">\n" +
                getInterproBox(vis) + "\n" +
                "</div>\n" +
                "<div class=\"column\" style=\"background-color:#F0F0F0;\">\n" +
                getTranscriptBox(vis) + "\n" +
                "</div>\n" +
                "</div>\n" +
                "<div class=\"svgrow\">\n" +
                vis.getIsoformSvg() +
                "</div>\n" +
                "<div class=\"svgrow\">\n" +
                vis.getProteinSvg() +
                "</div>\n" +
                "</article>\n";
    }

}
