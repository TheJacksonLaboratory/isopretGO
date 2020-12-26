package org.jax.isopret.visualization;

import java.util.List;

public class Util {


    public static String fromList(List<String> genes, String title) {
        StringBuilder sb = new StringBuilder();
        sb.append("<h3>").append(title).append("</h3>\n");
        sb.append("<ol>\n");
        for (String gene : genes) {
            sb.append("<li>").append(gene).append("</li>\n");
        }
        sb.append("</ol>\n");
        return sb.toString();
    }
}
