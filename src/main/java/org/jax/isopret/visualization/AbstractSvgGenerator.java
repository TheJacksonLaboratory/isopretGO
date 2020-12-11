package org.jax.isopret.visualization;

import java.io.IOException;
import java.io.Writer;

public abstract class AbstractSvgGenerator {

    protected final int SVG_WIDTH;
    protected final int SVG_HEIGHT;

    protected final static String PURPLE = "#790079";
    protected final static String GREEN = "#00A087";
    protected final static String DARKGREEN = "#006600";
    protected final static String RED ="#e64b35";
    protected final static String BLACK = "#000000";
    protected final static String NEARLYBLACK = "#040C04";
    protected final static String BLUE ="#4dbbd5";
    protected final static String BROWN="#7e6148";
    protected final static String DARKBLUE = "#3c5488";
    protected final static String VIOLET = "#8333ff";
    protected final static String ORANGE = "#ff9900";
    protected final static String BRIGHT_GREEN = "#00a087";
    protected final static String YELLOW = "#FFFFE0"; //lightyellow
    protected final static String LIGHT_GREY = "#D3D3D3";



    public AbstractSvgGenerator(int width, int height) {
        this.SVG_WIDTH = width;
        this.SVG_HEIGHT = height;
    }

    abstract void write(Writer writer);
    abstract String getSvg();


    /**
     * Write the header of the SVG.
     * @param writer file handle
     * @param blackBorder if true, write a black border around the SVG
     * @throws IOException if we cannot writ the SVG
     */
    protected void writeHeader(Writer writer, boolean blackBorder) throws IOException {
        writer.write("<svg width=\"" + SVG_WIDTH + "\" height=\"" + this.SVG_HEIGHT + "\" ");
        if (blackBorder) {
            writer.write("style=\"border:1px solid black\" ");
        }
        writer.write(
                "xmlns=\"http://www.w3.org/2000/svg\" " +
                        "xmlns:svg=\"http://www.w3.org/2000/svg\">\n");
        writer.write("<!-- Created by Isopret -->\n");
        writer.write("<style>\n" +
                "  text { font: 24px; }\n" +
                "  text.t20 { font: 20px; }\n" +
                "  text.t14 { font: 14px; }\n");
        writer.write("  .mytriangle{\n" +
                "    margin: 0 auto;\n" +
                "    width: 100px;\n" +
                "    height: 100px;\n" +
                "  }\n" +
                "\n" +
                "  .mytriangle polygon {\n" +
                "    fill:#b31900;\n" +
                "    stroke:#65b81d;\n" +
                "    stroke-width:2;\n" +
                "  }\n");
        writer.write("  </style>\n");
        writer.write("<g>\n");
    }

    protected void writeHeader(Writer writer) throws IOException {
        writeHeader(writer, true);
    }

    /**
     * Write the footer of the SVG
     */
    protected void writeFooter(Writer writer) throws IOException {
        writer.write("</g>\n</svg>\n");
    }

    /**
     * If there is some IO Exception, return an SVG with a text that indicates the error
     *
     * @param msg The error
     * @return An SVG element that contains the error
     */
    protected String getSvgErrorMessage(String msg) {
        return String.format("<svg width=\"200\" height=\"100\" " +
                "xmlns=\"http://www.w3.org/2000/svg\" " +
                "xmlns:svg=\"http://www.w3.org/2000/svg\">\n" +
                "<!-- Created by SvAnna -->\n" +
                "<g><text x=\"10\" y=\"10\">%s</text>\n</g>\n" +
                "</svg>\n", msg);
    }

}
