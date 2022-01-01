package org.jax.isopret.core.visualization;

import java.util.List;

public class HtmlGoAnnotationMatrix {


    public HtmlGoAnnotationMatrix(List<GoAnnotationRow> rows) {
        StringBuilder sb = new StringBuilder();
        sb.append(HTML_HEADER);

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
            </style>
            <body>
            """;

    private static final String HTML_FOOTER = """
            </body>
            </html>
            """;


}
