package org.jax.isopret.visualization;

public class SvgUtil {


    public static String square(double x, double y, double dim, String fillColor) {
        return String.format("<rect x=\"%f\" y=\"%f\" width=\"%f\" height=\"%f\" " +
                        "style=\"fill:%s;stroke:black;stroke-width:1\" />\n",
                x, y, dim, dim, fillColor);
    }

    public static String text(double x, double y, String textColor, int textPx, String message) {
        return String.format("<text x=\"%f\" y=\"%f\" style=\"fill:%s;font-size:%dpx\">%s</text>\n",
                x, y, textColor, textPx, message);
    }
}
