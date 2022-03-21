package org.jax.isopret.core.visualization;

import org.jax.isopret.core.model.Transcript;

import java.util.List;

/**
 * This class creates an HTML table with the GO annotations of
 * a gene for all of its expressed transcripts. GO terms that
 * are significant for the study set are marked green.
 * @author Peter N Robinson
 */
public class HtmlGoAnnotationMatrixVisualizer {

    private final String html;

    /**
     *
     * Build a table with go annotations without HTML header or foter
     */
    public HtmlGoAnnotationMatrixVisualizer(GoAnnotationMatrix matrix, List<Transcript> expressedTranscripts) {
        if (matrix.getAllGoIds().isEmpty()) {
            this.html = "<p>No Gene Ontology annotations found.</p>";
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append(htmlTableHeader(matrix.getExpressedTranscripts()));
            for (GoAnnotationRow row : matrix.getExpressedAnnotationRows()) {
                sb.append(getRow(row));
            }
            sb.append("</table>\n");
            html = sb.toString();
        }
    }

    private String getRow(GoAnnotationRow row) {
        String go = String.format("%s (%s)", row.getGoLabel(), row.getGoId());
        StringBuilder sb = new StringBuilder();
        if (row.isGoTermSignificant()) {
            sb.append("<tr style=\"background-color:#90EE90\">");
        } else {
            sb.append("<tr>");
        }
        sb.append("<td>").append(go).append("</td>");
        for (boolean b : row.getTranscriptAnnotated()) {
            if (b) {
                sb.append("<td>&#10003;</td>");
            } else {
                sb.append("<td>-</td>");
            }
        }
        sb.append("</tr>");
        return sb.toString();
    }

    private String htmlTableHeader(List<String> transcripts) {
        StringBuilder sb = new StringBuilder();
        sb.append("<table class=\"go\">");
        sb.append("<tr>");
        sb.append("<th width=\"375px\";>GO term</th>");
        for (String transcript : transcripts) {
            sb.append("<th width=\"28px\";><span>").append(transcript).append("</span></th>");
        }
        sb.append("</tr>");
        return sb.toString();
    }

    private final static String HTML_HEADER = """
            <!doctype html>
            <html class="no-js" lang="">

            <head>
              <meta charset="utf-8">
              <meta http-equiv="x-ua-compatible" content="ie=edge">
               <style>
            html, body {
               padding: 0;
               margin: 20;
               font-size:14px;
            }

            body {
               font-family:"DIN Next", Helvetica, Arial, sans-serif;
               line-height:1.25;
               background-color:white   ;
                max-width:1200px;
                margin-left:auto;
                margin-right:auto;
             }
             
             gotable, gotable.th, gotable.td {
                 border: 0.5px solid;
                 border-collapse: collapse;
             }

             gotable.th
             {
               vertical-align: bottom;
               text-align: center;
             }
             
             gotable.th span
             {
               -ms-writing-mode: tb-rl;
               -webkit-writing-mode: vertical-rl;
               writing-mode: vertical-rl;
               transform: rotate(180deg);
               white-space: nowrap;
               padding: 5px 8px;
                margin: 0 auto;
             }
            </style>
            <body>
            """;

    private static final String HTML_FOOTER = """
            </body>
            </html>
            """;

    public String getHtml() {
        return html;
    }

    public String getWrappedHtml() {
        return HTML_HEADER + html + HTML_FOOTER;
    }

}
