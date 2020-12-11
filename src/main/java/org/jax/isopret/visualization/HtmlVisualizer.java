package org.jax.isopret.visualization;

public class HtmlVisualizer implements Visualizer {



    public HtmlVisualizer() {

    }

    @Override
    public String getHtml(Visualizable vis) {
        StringBuilder sb = new StringBuilder();
        sb.append("<h1>").append(vis.getGeneSymbol()).append(" &emsp; ").append("</h1>\n");
        sb.append("<div class=\"row\">\n");
        sb.append("<div class=\"column\" style=\"background-color:#F8F8F8;\">\n");
        sb.append("<h2>Sequence</h2>\n");
        //sb.append(getSequencePrioritization(visualizable)).append("\n");
        sb.append("</div>\n");
        sb.append("<div class=\"column\" style=\"background-color:#F0F0F0;\">\n");
        sb.append("<h2>Phenotypic data</h2>\n");
       // sb.append(getPhenotypePrioritization(visualizable)).append("\n");
        sb.append("</div>\n");
        sb.append("</div>\n");
       // String svg = getSvgString(visualizable);
       // sb.append(svg);

        return sb.toString();

    }
}
