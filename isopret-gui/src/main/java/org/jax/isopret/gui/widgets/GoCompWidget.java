package org.jax.isopret.gui.widgets;

import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
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

    private final static String dgeTitle = "GO Terms with predominant overrepresentation in DGE";
    private final static String dasTitle = "GO Terms with predominant overrepresentation in DAS";
    private final static String dgeBody = """
            These GO terms displayed a higher degree of overexpression in the DGE set than in the DAS set
            """;
    private final static String dasBody = """
            These GO terms displayed a higher degree of overexpression in the DAS set than in the DGE set
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
        for (GoCompTerm goComp : goComparison.getGoCompTermList()) {
            String label = goComp.getLabel();
            double dge = goComp.getDge();
            double das = goComp.getDas();
            LOGGER.info("GoComp dge {} das {}", dge, das);
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
        BarChart<Number, String> barChart = getBarChart(goTerms);
        ScrollPane pane = new ScrollPane(barChart);
        return new VBox(titleText, bodyText, pane);
    }



    public void show(Stage primaryStage) {

       HBox hbox = new HBox();

        VBox dgeVbox = getBarChartPane("DGE", goComparison.getDgePredominentGoCompTerms());
       VBox dasVbox = getBarChartPane("DAS", goComparison.getDasPredominentGoCompTerms());
       hbox.getChildren().addAll(dgeVbox, dasVbox);
        Scene scene = new Scene(hbox, 600, 1200);

        Stage newWindow = new Stage();
        newWindow.setTitle("Gene Ontology: DGE vs. DAS Overenrichment");
        newWindow.setScene(scene);

        // Set position of second window, related to primary window.
        newWindow.setX(primaryStage.getX() + 200);
        newWindow.setY(primaryStage.getY() + 100);



        newWindow.show();
    }


}
