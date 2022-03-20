package org.jax.isopret.gui.widgets;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.*;
import javafx.stage.Stage;
import org.jax.isopret.gui.service.model.GoCompTerm;
import org.jax.isopret.gui.service.model.GoComparison;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


public class GoCompWidget {
    private final Logger LOGGER = LoggerFactory.getLogger(GoCompWidget.class);
    private final GoComparison goComparison;
    private final int nSignificantGoTerms;

    private final static String dgeTitle = "DGE-predominant overrepresentation\n";
    private final static String dasTitle = "DAS-predominant overrepresentation\n";
    private final static String dgeBody = """
            These GO terms displayed a higher degree of overexpression in the set of differentially expressed
            genes set than in the set of differentially expressed transcripts.
            """;
    private final static String dasBody = """
            These GO terms displayed a higher degree of overexpression in the set of differentially
            expressed transcripts set than in the DGE set of differentially expressed genes.
            """;



    public GoCompWidget(GoComparison comparison) {
        goComparison = comparison;
        nSignificantGoTerms = comparison.getGoCompTermList().size();
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
        XYChart.Series<Number, String> dataSeriesDGE = new XYChart.Series<>();
        dataSeriesDGE.setName("DGE");
        XYChart.Series<Number, String> dataSeriesDAS = new XYChart.Series<>();
        dataSeriesDAS.setName("DAS");
        for (GoCompTerm goComp : goTerms) {
            String label = goComp.getLabel();
            double dge = goComp.getDge();
            double das = goComp.getDas();
            dataSeriesDGE.getData().add(new XYChart.Data<>(dge,label));
            dataSeriesDAS.getData().add(new XYChart.Data<>(das, label));
        }
        barChart.getData().add(dataSeriesDGE);
        barChart.getData().add(dataSeriesDAS);
        return barChart;
    }

    private VBox getBarChartPane(String type, List<GoCompTerm> goTerms) {
        TextFlow text_flow = new TextFlow();
        Text titleText;
        Text bodyText;
        if (type.equals("DGE")) {
            titleText = new Text(dgeTitle);
            bodyText = new Text(dgeBody);
        } else {
            titleText = new Text(dasTitle);
            bodyText = new Text(dasBody);
        }
        titleText.setFont(Font.font("Verdana", FontPosture.ITALIC,16));
        titleText.setStyle(".break-word { word-wrap: break-word; }");
        text_flow.getChildren().addAll(titleText, bodyText);
        BarChart<Number, String> barChart = getBarChart(goTerms);
        ScrollPane pane = new ScrollPane(barChart);
        int n_terms = goTerms.size();
        LOGGER.info("{} with {} go terms for display", type, n_terms);
        return new VBox(text_flow, pane);
    }



    public void show(Stage primaryStage) {

       HBox hbox = new HBox();

       VBox dgeVbox = getBarChartPane("DGE", goComparison.getDgePredominentGoCompTerms());
       dgeVbox.setSpacing(15);
       VBox dasVbox = getBarChartPane("DAS", goComparison.getDasPredominentGoCompTerms());
       dasVbox.setSpacing(15);
       Region r = new Region();
       r.setMinWidth(20);
       HBox.setHgrow(r, Priority.ALWAYS);
       hbox.getChildren().addAll(dgeVbox, r, dasVbox);
       Font plain = Font.font("TimeRoman", 16);
       Font bold = Font.font("TimesRoman", FontWeight.BOLD, FontPosture.ITALIC, 16);
       Text text1 = new Text("Gene Ontology (GO) overenrichment analysis was performed using ");
       Text text2 = new Text(goComparison.goMethod());
       Text text3 = new Text(" and ");
       Text text4 = new Text(goComparison.mtcMethod());
       Text text5= new Text(". The negative decadic logarithm of the p-value for enrichment is "+
               "displayed separately for GO terms with predominant enrichment in the set of "+
               "differentially expressed genes (DGE) and the set of differential alternative "+
               "splicing (DAS), i.e., differentially spliced transcripts.");
       text1.setFont(plain);
       text2.setFont(bold);
       text3.setFont(plain);
       text4.setFont(bold);
       text5.setFont(plain);
       TextFlow tflow = new TextFlow(text1, text2, text3, text4, text5);
       VBox vb = new VBox(20, hbox, tflow);
       vb.setPadding(new Insets(20, 20, 10, 20));
       Scene scene = new Scene(vb, 1200, 800);

        Stage newWindow = new Stage();
        newWindow.setTitle("Gene Ontology: DGE vs. DAS Overenrichment");
        newWindow.setScene(scene);

        // Set position of second window, related to primary window.
        newWindow.setX(primaryStage.getX() + 200);
        newWindow.setY(primaryStage.getY() + 100);

        newWindow.show();
    }


}
