package org.jax.isopret.core.visualization;

import java.util.List;

public class HtmlGoAnnotationMatrix {

    private final String html;
    private final String allTranscriptsHtml;



    public HtmlGoAnnotationMatrix(GoAnnotationMatrix matrix) {
        StringBuilder sb = new StringBuilder();
        sb.append(HTML_HEADER);
        sb.append(htmlTableHeader(matrix.getTranscripts()));
        for (GoAnnotationRow row : matrix.getAnnotationRows()) {
            sb.append(getRow(row));
        }
        sb.append(HTML_FOOTER);
        allTranscriptsHtml = sb.toString();
        sb = new StringBuilder();
        sb.append(HTML_HEADER);
        sb.append(htmlTableHeader(matrix.getExpressedTranscripts()));
        for (GoAnnotationRow row : matrix.getExpressedAnnotationRows()) {
            sb.append(getRow(row));
        }
        sb.append(HTML_FOOTER);
        html = sb.toString();
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
        sb.append("<table>");
        sb.append("<tr>");
        sb.append("<th>GO term</th>");
        for (String transcript : transcripts) {
            sb.append("<th><span>").append(transcript).append("</span></th>");
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
             th
             {
               vertical-align: bottom;
               text-align: center;
             }
             
             th span
             {
               -ms-writing-mode: tb-rl;
               -webkit-writing-mode: vertical-rl;
               writing-mode: vertical-rl;
               transform: rotate(180deg);
               white-space: nowrap;
               padding: 5px 10px;
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

    public String getHtmlAllTranscripts() {
        return allTranscriptsHtml;
    }
}
