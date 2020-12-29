package org.jax.isopret.visualization;

import org.jax.isopret.except.IsopretRuntimeException;
import org.jax.isopret.go.GoTermIdPlusLabel;

import java.util.List;
import java.util.Map;

public class HtmlVisualizer implements Visualizer {

    private final Map<String, String> prositeIdToName;

    public HtmlVisualizer(Map<String, String> prositeIdToName) {
        this.prositeIdToName = prositeIdToName;
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
        sb.append(String.format("<td>%.2f</td>",  vis.getExpressionLogFoldChange()));
        sb.append("<td>").append(vis.getExpressionPval()).append("</td></tr>\n");
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

    private final static String PROSITE_TABLE_HEADER = "<table>\n" +
            "  <thead>\n" +
            "    <tr>\n" +
            "      <th>Prosite id</th>\n" +
            "      <th>Name</th>\n" +
            "    </tr>\n" +
            "  </thead>\n";

    public String getPrositeBox(Visualizable vis) {
        StringBuilder sb = new StringBuilder();
        List<List<String>> prositeLinks = vis.getPrositeModuleLinks(this.prositeIdToName);
        if (prositeLinks.isEmpty()) {
            return "<p><i>No protein domains found.</i></p>\n";
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

    @Override
    public String getHtml(Visualizable vis) {
        StringBuilder sb = new StringBuilder();
        sb.append("<h1>").append(vis.getGeneSymbol()).append(" &emsp; ").append("</h1>\n");
        sb.append("<div class=\"generow\">\n");
        sb.append("<div class=\"column\" style=\"background-color:#F8F8F8;\">\n");
        sb.append(getGeneBox(vis)).append("\n");
        sb.append("</div>\n");
        sb.append("<div class=\"column\" style=\"background-color:#F0F0F0;\">\n");
        sb.append(getPrositeBox(vis)).append("\n");
        sb.append("</div>\n");
        sb.append("<div class=\"column\" style=\"background-color:#F0F0F0;\">\n");
        sb.append(getTranscriptBox(vis)).append("\n");
       sb.append("</div>\n");
        sb.append("</div>\n");
        sb.append("<div class=\"svgrow\">\n");
       sb.append(vis.getIsoformSvg());
        sb.append("</div>\n");
        sb.append("<div class=\"svgrow\">\n");
        sb.append(vis.getProteinSvg(this.prositeIdToName));
        sb.append("</div>\n");
        return sb.toString();
    }

}
