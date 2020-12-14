package org.jax.isopret.visualization;

import org.jax.isopret.except.IsopretRuntimeException;

import java.util.List;
import java.util.Map;

public class HtmlVisualizer implements Visualizer {

    private final Map<String, String> prositeIdToName;

    public HtmlVisualizer(Map<String, String> prositeIdToName) {
        this.prositeIdToName = prositeIdToName;
    }


    private final static String PROSITE_TABLE_HEADER = "<table>\n" +
            "  <thead>\n" +
            "    <tr>\n" +
            "      <th>Prosite id</th>\n" +
            "      <th>Name</th>\n" +
            "    </tr>\n" +
            "  </thead>\n";

    public String getGeneBox(Visualizable vis) {
        StringBuilder sb = new StringBuilder();
        sb.append("<p>").append(vis.getGeneSymbol()).append("</p>\n");
        sb.append("<p>").append(vis.getChromosome()).append("</p>\n");
        String a = String.format("<a href=\"%s\" target=\"__blank\">%s</a>\n", vis.getGeneUrl(), vis.getGeneAccession());
        sb.append("<p>").append(a).append("</p>\n");
        sb.append(String.format("<p>Fold change: %.2f (log fold change: %.2f)</p>\n", vis.getExpressionFoldChange(), vis.getExpressionLogFoldChange()));
        sb.append("<p>P-value: ").append(vis.getExpressionPval()).append("</p>\n");
        List<List<String>> prositeLinks = vis.getPrositeModuleLinks(this.prositeIdToName);
        if (prositeLinks.isEmpty()) {
            return sb.toString();
        }
        sb.append(PROSITE_TABLE_HEADER);
        for (var row : prositeLinks) {
            if (row.size() != 2) {
                // should never happen!
                throw new IsopretRuntimeException("Malformed prosite row: " + row);
            }
            sb.append("<tr><td>").append(row.get(0)).append("</td>");
            sb.append("<td>").append(row.get(1)).append("</td></tr>\n");
        }
        sb.append("</table>\n");
        return sb.toString();
    }

    private final static String ISOFORM_TABLE_HEADER = "<table>\n" +
            "  <thead>\n" +
            "    <tr>\n" +
            "      <th>Isoform</th>\n" +
            "      <th>Log<sub>2</sub> fold change</th>\n" +
            "      <th>P-value</th>\n" +
            "      <th>Corrected P-value</th>\n" +
            "    </tr>\n" +
            "  </thead>\n";

    private String getTranscriptBox(Visualizable vis) {
        StringBuilder sb = new StringBuilder();
        List<List<String>> tableData = vis.getIsoformTableData();
        if (tableData.isEmpty()) {
            return "<p>No isoform data found.</p>\n";
        }
        int totalIsoforms = vis.getTotalTranscriptCount();
        int expressionIsoforms = vis.getExpressedTranscriptCount();
        sb.append("<p>").append(expressionIsoforms).append(" transcripts were expressed in the data from ")
                .append(totalIsoforms).append(" annotated transcripts.</p>\n");
        sb.append(ISOFORM_TABLE_HEADER);
        for (var row : tableData) {
            if (row.size() != 4) {
                // should never happen!
                throw new IsopretRuntimeException("Malformed isoform row: " + row);
            }
            sb.append("<tr><td>").append(row.get(0)).append("</td>");
            sb.append("<td>").append(row.get(1)).append("</td>");
            sb.append("<td>").append(row.get(2)).append("</td>");
            sb.append("<td>").append(row.get(3)).append("</td></tr>\n");
        }
        sb.append("</table>\n");
        return sb.toString();
    }

    @Override
    public String getHtml(Visualizable vis) {
        StringBuilder sb = new StringBuilder();
        sb.append("<h1>").append(vis.getGeneSymbol()).append(" &emsp; ").append("</h1>\n");
        sb.append("<div class=\"row\">\n");
        sb.append("<div class=\"column\" style=\"background-color:#F8F8F8;\">\n");
        sb.append("<h2>Gene</h2>\n");
        sb.append(getGeneBox(vis)).append("\n");
        sb.append("</div>\n");
        sb.append("<div class=\"column\" style=\"background-color:#F0F0F0;\">\n");
        sb.append("<h2>Transcripts</h2>\n");
        sb.append(getTranscriptBox(vis)).append("\n");
       sb.append("</div>\n");
        sb.append("</div>\n");
        sb.append("<div class=\"row\">\n");
       sb.append(vis.getIsoformSvg());
        sb.append("</div>\n");
        sb.append("<div class=\"row\">\n");
        sb.append(vis.getProteinSvg(this.prositeIdToName));
        sb.append("</div>\n");
        return sb.toString();

    }
}
