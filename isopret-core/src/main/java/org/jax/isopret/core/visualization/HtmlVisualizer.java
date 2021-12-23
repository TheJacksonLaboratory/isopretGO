package org.jax.isopret.core.visualization;

import org.jax.isopret.core.except.IsopretRuntimeException;
import org.jax.isopret.core.go.GoTermIdPlusLabel;
import org.jax.isopret.core.interpro.DisplayInterproAnnotation;
import org.jax.isopret.core.interpro.InterproEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class HtmlVisualizer implements Visualizer {
    private static final Logger LOGGER = LoggerFactory.getLogger(HtmlVisualizer.class);
    public HtmlVisualizer() {
    }


    private final static String GENE_TABLE_HEADER = "<table>\n" +
            "  <thead>\n" +
            "    <tr>\n" +
            "      <th>Gene</th>\n" +
            "      <th>Chrom.</th>\n" +
            "      <th>Log<sub>2</sub> fold change</th>\n" +
            "      <th>Probability (PEP)</th>\n" +
            "    </tr>\n" +
            "  </thead>\n";


    private String getGoAnchor(GoTermIdPlusLabel go) {
        //QuickGO - https://www.ebi.ac.uk/QuickGO/term/GO:0006915
        String url = "https://www.ebi.ac.uk/QuickGO/term/" + go.getId();
        return String.format("<a href=\"%s\" target=\"_blank\">%s</a>\n", url, go.getLabel());
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
        List<GoTermIdPlusLabel> goterms = vis.getGoTerms();
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

    private final static String INTERPRO_TABLE_HEADER = "<table>\n" +
            "  <thead>\n" +
            "    <tr>\n" +
            "      <th>Interpro id</th>\n" +
            "      <th>Name</th>\n" +
            "      <th>Type</th>\n" +
            "    </tr>\n" +
            "  </thead>\n";

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


    private final static String ISOFORM_TABLE_HEADER = "<table>\n" +
            "  <thead>\n" +
            "    <tr>\n" +
            "      <th>Isoform</th>\n" +
            "      <th>Log<sub>2</sub> fold change</th>\n" +
            "      <th>Probability (PEP)</th>\n" +
            "    </tr>\n" +
            "  </thead>\n";

    private String getTranscriptBox(Visualizable vis) {
        final int EXPECTED_N_COLUMNS = 3;
        StringBuilder sb = new StringBuilder();
        List<List<String>> tableData = vis.getIsoformTableData();
        if (tableData.isEmpty()) {
            return "<p>No isoform data found.</p>\n";
        }
        int totalIsoforms = vis.getTotalTranscriptCount();
        int expressionIsoforms = vis.getExpressedTranscriptCount();
        sb.append(ISOFORM_TABLE_HEADER);
        for (var row : tableData) {
            if (row.size() != EXPECTED_N_COLUMNS) {
                // should never happen!
                throw new IsopretRuntimeException("Malformed isoform row: " + row);
            }
            sb.append("<tr><td>").append(row.get(0)).append("</td>");
            sb.append("<td>").append(row.get(1)).append("</td>");
            sb.append("<td>").append(row.get(2)).append("</td></tr>\n");
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
        StringBuilder sb = new StringBuilder();
        sb.append(getGeneNameAndBadges(vis));
        sb.append("<article>\n");
        sb.append("<div class=\"generow\">\n");
        sb.append("<div class=\"column\" style=\"background-color:#F8F8F8;\">\n");
        sb.append(getGeneBox(vis)).append("\n");
        sb.append("</div>\n");
        sb.append("<div class=\"column\" style=\"background-color:#F0F0F0;\">\n");
        sb.append(getInterproBox(vis)).append("\n");
        sb.append("</div>\n");
        sb.append("<div class=\"column\" style=\"background-color:#F0F0F0;\">\n");
        sb.append(getTranscriptBox(vis)).append("\n");
        sb.append("</div>\n");
        sb.append("</div>\n");
        sb.append("<div class=\"svgrow\">\n");
        sb.append(vis.getIsoformSvg());
        sb.append("</div>\n");
        sb.append("<div class=\"svgrow\">\n");
        sb.append(vis.getProteinSvg());
        sb.append("</div>\n");
        sb.append("</article>\n");
        return sb.toString();
    }

}
