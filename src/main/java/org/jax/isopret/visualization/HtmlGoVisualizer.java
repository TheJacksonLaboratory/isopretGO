package org.jax.isopret.visualization;

import java.util.List;

public class HtmlGoVisualizer {


    private final List<GoVisualizable> goTerms;


    public HtmlGoVisualizer(List<GoVisualizable> goTerms) {
        this.goTerms = goTerms;
    }


    private final static String GO_TABLE_HEADER = "<table>\n" +
            "  <thead>\n" +
            "    <tr>\n" +
            "      <th>GO id</th>\n" +
            "      <th>Name</th>\n" +
            "      <th>Study</th>\n" +
            "      <th>Population</th>\n" +
            "      <th>p-value</th>\n" +
            "    </tr>\n" +
            "  </thead>\n";



    private String countsCell(int annot, int total) {
        double percentage = 100.0 * (double)annot/(double)total;
        return String.format("%d/%d (%.1f%%)", annot, total, percentage);
    }

    private String pvalFormat(double p) {
        if (p>=0.01) {
            return String.format("%.3f", p);
        }
        int exponent = (int)Math.floor(Math.log10(p));
        double mantissa = p / Math.pow(10, exponent);
        return String.format("%.2f &times; 10<sup>%d</sup>", mantissa, exponent);
    }


    public String getHtml() {
        StringBuilder sb = new StringBuilder();
        sb.append(GO_TABLE_HEADER);
        for (GoVisualizable v : goTerms) {
            String pvalCell = String.format("%s (adj.: %s)", pvalFormat(v.pvalue()), pvalFormat(v.adjustedPvalue()));
            sb.append("<tr>")
                    .append("<td>").append(v.termId()).append("</td>")
                    .append("<td>").append(v.termLabel()).append("</td>")
                    .append("<td>").append(countsCell(v.studyCount(), v.studyTotal())).append("</td>")
                    .append("<td>").append(countsCell(v.populationCount(), v.populationTotal())).append("</td>")
                    .append("<td>").append(pvalCell).append("</td>")
                    .append("</td>");
        }
        sb.append("</table>");
        return sb.toString();
    }

}
