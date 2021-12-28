package org.jax.isopret.core.visualization;

import java.util.List;

public class HtmlGoVisualizer {


    private final List<GoVisualizable> goTerms;
    /**
     * An identified that will be used to identify this table by JavaScript.
     */
    private final String id;

    public HtmlGoVisualizer(List<GoVisualizable> goTerms, String id) {
        this.goTerms = goTerms;
        this.id = id;
    }


    /**
     * String for the header of a gene ontology table.
     * Note that there is a placeholder for the id, which is used by the JavScript to identify
     * the dasTable, dgeTable
     */
    private final static String GO_TABLE_HEADER = """
            <table class="goTable" id="%s">
              <thead>
                <tr>
                  <th>GO id</th>
                  <th>Name</th>
                  <th>Study</th>
                  <th>Population</th>
                  <th>p-value</th>
                  <th>p-value (adj.)</th>
                </tr>
              </thead>
            """;


    private String countsCell(int annot, int total) {
        double percentage = 100.0 * (double) annot / (double) total;
        return String.format("%d/%d (%.1f%%)", annot, total, percentage);
    }

    private String pvalFormat(double p) {
        if (p >= 0.01) {
            return String.format("%.3f", p);
        }
        int exponent = (int) Math.floor(Math.log10(p));
        double mantissa = p / Math.pow(10, exponent);
        return String.format("%.2f &times; 10<sup>%d</sup>", mantissa, exponent);
    }


    public String getHtml() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format(GO_TABLE_HEADER, id));
        for (GoVisualizable v : goTerms) {
            sb.append("<tr>")
                    .append("<td>").append(v.termId()).append("</td>")
                    .append("<td>").append(v.termLabel()).append("</td>")
                    .append("<td>").append(countsCell(v.studyCount(), v.studyTotal())).append("</td>")
                    .append("<td>").append(countsCell(v.populationCount(), v.populationTotal())).append("</td>")
                    .append("<td>").append(pvalFormat(v.pvalue())).append("</td>")
                    .append("<td>").append(pvalFormat(v.adjustedPvalue())).append("</td>")
                    .append("</tr>");
        }
        sb.append("</table>");
        return sb.toString();
    }

}
