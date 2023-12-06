package org.jax.isopret.gui.widgets;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.jax.isopret.gui.service.model.GeneOntologyComparisonMode;
import org.jax.isopret.gui.service.model.GoCompTerm;
import org.jax.isopret.gui.service.model.GoComparison;
import org.jax.isopret.model.GoMethod;
import org.jax.isopret.model.MtcMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A widget to display either DAS or DGE enriched GO terms
 */
public class GoDisplayWidget implements GoWidget{
    private final Logger LOGGER = LoggerFactory.getLogger(GoDisplayWidget.class);
    private final List<GoCompTerm> sigGoTerms;

    private final static String dgeTitle = "GO Terms with DGE overrepresentation\n";
    private final static String dasTitle = "GO Terms with DAS overrepresentation\n";
    private final static String dgeBody = """
            These GO terms displayed overrepresentation in the set of differentially expressed genes (DGE).
            """;
    private final static String dasBody = """
            These GO terms displayed overrepresentation in the set of differentially alternatively spliced transcripts (DAS).
            """;

    private final String title;
    private final String body;

    private final GoMethod goMethod;
    private final MtcMethod mtcMethod;

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


    BarChart<Number, String> getBarChart(List<GoCompTerm> goTerms) {
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
            String label = limitLabelLength(goComp.getLabel());
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
        Label  label = new Label(this.body);
        label.setWrapText(true);
        label.setStyle("-fx-font: 12pt Arial");
        BarChart<Number, String> barChart = getBarChart(this.sigGoTerms);
        ScrollPane pane = new ScrollPane(barChart);
        int n_terms = this.sigGoTerms.size();
        LOGGER.info("{} GO terms for display",  n_terms);
        return new VBox(label, pane);
    }

    public void show(Stage window) {
        HBox hbox = new HBox();
        VBox dgeVbox = getBarChartPane();
        dgeVbox.setSpacing(15);
        hbox.getChildren().addAll(dgeVbox);
        String sb = "Gene Ontology (GO) overrepresentation analysis was performed using the " +
                goMethod.longNameWithAbbreviation() + " approach with " + mtcMethod.display() +
                " multiple-testing correction. " +
                "The negative decadic logarithm of the p-value for enrichment is " +
                "displayed for GO terms.";
        Label label = new Label(sb);
        label.setWrapText(true);
        label.setStyle("-fx-font: 12pt Arial");
        VBox vb = new VBox(20, hbox, label);
        vb.setPadding(new Insets(20, 20, 10, 20));
        Scene scene = new Scene(vb, 1200, 800);

        Stage newWindow = new Stage();
        newWindow.setTitle(this.title);
        newWindow.setScene(scene);

        // Set position of second window, related to primary window.
        newWindow.setX(window.getX() + 200);
        newWindow.setY(window.getY() + 100);

        newWindow.show();
    }
}
