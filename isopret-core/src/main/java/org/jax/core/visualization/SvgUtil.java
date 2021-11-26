package org.jax.core.visualization;

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
    public static String boldItalicText(double x, double y, String textColor, int textPx, String message) {
        return String.format("<text x=\"%f\" y=\"%f\" style=\"fill:%s;font-style:italic;font-weight:bold;font-size:%dpx\">%s</text>\n",
                x, y, textColor, textPx, message);
    }

    public static String unfilledBox(double x, double y, double width, double height, String strokeColor) {
        return String.format("<rect x=\"%f\" y=\"%f\" width=\"%f\" height=\"%f\" " +
                        "style=\"stroke:%s; fill:none \" />\n",
                x, y, width, height, strokeColor);
    }

    public static String filledBox(double x, double y, double width, double height, String strokeColor, String fillColor) {
        return String.format("<rect x=\"%f\" y=\"%f\" width=\"%f\" height=\"%f\" " +
                        "style=\"stroke:%s; fill:%s \" />\n",
                x, y, width, height, strokeColor, fillColor);
    }

    public static String unfilledBox(double x, double y, double width, double height) {
        return unfilledBox(x,y,width,height, "black");
    }

    public static String line(double x1, double y1, double x2, double y2) {
        return String.format("<line x1=\"%f\" y1=\"%f\" x2=\"%f\" y2=\"%f\" stroke=\"black\"/>\n",
                x1, y1, x2, y2);
    }


}
