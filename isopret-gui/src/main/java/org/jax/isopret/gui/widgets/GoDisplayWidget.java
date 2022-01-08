package org.jax.isopret.gui.widgets;

import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.*;
import javafx.stage.Stage;
import org.jax.isopret.gui.service.model.GeneOntologyComparisonMode;
import org.jax.isopret.gui.service.model.GoCompTerm;
import org.jax.isopret.gui.service.model.GoComparison;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * A widget to display either DAS or DGE enriched GO terms
 */
public class GoDisplayWidget {
    private final Logger LOGGER = LoggerFactory.getLogger(GoDisplayWidget.class);
    private final List<GoCompTerm> sigGoTerms;

    private final static String dgeTitle = "GO Terms with DGE overrepresentation\n";
    private final static String dasTitle = "GO Terms with DAS overrepresentation\n";
    private final static String dgeBody = """
            These GO terms displayed overexpression in the set of differentially expressed genes set.
            """;
    private final static String dasBody = """
            These GO terms displayed overexpression in the set of differentially expressed transcripts. 
            """;

    private final String title;
    private final String body;

    private final String goMethod;
    private final String mtcMethod;

    private final GeneOntologyComparisonMode compMode;





    public GoDisplayWidget(GoComparison comparison, GeneOntologyComparisonMode mode) {
        if (mode.equals(GeneOntologyComparisonMode.DGE)) {
            this.title = dgeTitle;
            this.body = dgeBody;
            this.sigGoTerms = comparison.getDgeSignificant();
        } else {
            this.title = dasTitle;
            this.body = dasBody;
            this.sigGoTerms = comparison.getDasSignificant();
        }
        this.compMode = mode;
        this.goMethod = comparison.goMethod();
        this.mtcMethod = comparison.mtcMethod();
    }

    BarChart<Number, String > getBarChart(List<GoCompTerm> goTerms) {
        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("-log10(p-value)");
        CategoryAxis yAxis = new CategoryAxis();
        yAxis.setLabel("Gene Ontology term");
        BarChart<Number, String> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setBarGap(0);
        barChart.setCategoryGap(2.0);
        int height = 30 * goTerms.size() + 50;
        barChart.setMinHeight(height);
        XYChart.Series<Number, String> dataSeriesPvals = new XYChart.Series<>();
        dataSeriesPvals.setName(compMode.name());

        for (GoCompTerm goComp : goTerms) {
            String label = goComp.getLabel();
            double pval = switch (compMode) {
                case DAS -> goComp.getDas();
                case DGE -> goComp.getDge();
            };
            dataSeriesPvals.getData().add(new XYChart.Data<>(pval,label));
        }
        barChart.getData().add(dataSeriesPvals);
        return barChart;
    }

    private VBox getBarChartPane() {
        TextFlow text_flow = new TextFlow();
        Text titleText= new Text(this.title);
        Text bodyText  = new Text(this.body);
        titleText.setFont(Font.font("Verdana", FontPosture.ITALIC,16));
        titleText.setStyle(".break-word { word-wrap: break-word; }");
        text_flow.getChildren().addAll(titleText, bodyText);
        BarChart<Number, String> barChart = getBarChart(this.sigGoTerms);
        ScrollPane pane = new ScrollPane(barChart);
        int n_terms = this.sigGoTerms.size();
        LOGGER.info("{} go terms for display",  n_terms);
        return new VBox(text_flow, pane);
    }

    public void show(Stage window) {
        HBox hbox = new HBox();

        VBox dgeVbox = getBarChartPane();
        dgeVbox.setSpacing(15);;
        hbox.getChildren().addAll(dgeVbox);
        Font plain = Font.font("TimeRoman", 16);
        Font bold = Font.font("TimesRoman", FontWeight.BOLD, FontPosture.ITALIC, 16);
        Text text1 = new Text("Gene Ontology (GO) overenrichment analysis was performed using ");
        Text text2 = new Text(goMethod);
        Text text3 = new Text(" and ");
        Text text4 = new Text(mtcMethod);
        Text text5= new Text(". The negative decadic logarithm of the p-value for enrichment is "+
                "displayed for GO terms.");
        text1.setFont(plain);
        text2.setFont(bold);
        text3.setFont(plain);
        text4.setFont(bold);
        text5.setFont(plain);
        TextFlow tflow = new TextFlow(text1, text2, text3, text4, text5);
        VBox vb = new VBox(hbox, tflow);
        Scene scene = new Scene(vb, 1200, 800);

        Stage newWindow = new Stage();
        newWindow.setTitle("Gene Ontology: DGE vs. DAS Overenrichment");
        newWindow.setScene(scene);

        // Set position of second window, related to primary window.
        newWindow.setX(window.getX() + 200);
        newWindow.setY(window.getY() + 100);



        newWindow.show();
    }
}
