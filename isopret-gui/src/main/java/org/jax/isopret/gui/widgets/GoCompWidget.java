package org.jax.isopret.gui.widgets;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.jax.isopret.gui.service.model.GoCompTerm;
import org.jax.isopret.gui.service.model.GoComparison;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class GoCompWidget {
    Logger LOGGER = LoggerFactory.getLogger(GoCompWidget.class);
    private final GoComparison goComparison;
    private final int nSignificantGoTerms;



    public GoCompWidget(GoComparison comparison) {
        goComparison = comparison;
        nSignificantGoTerms = comparison.getGoCompTermList().size();
    }

    public void show(Stage primaryStage) {

        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("-log10(p-value)");
        CategoryAxis yAxis = new CategoryAxis();
        yAxis.setLabel("Gene Ontology term");
        BarChart<Number, String> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setBarGap(0);
        barChart.setCategoryGap(2.0);
        int height = 30 * nSignificantGoTerms + 50;
        barChart.setMinHeight(height);
        //set first bar color
        for(Node n:barChart.lookupAll(".default-color0.chart-bar")) {
            n.setStyle("-fx-bar-fill: #003f5c;");
        }
        //second bar color
        for(Node n:barChart.lookupAll(".default-color1.chart-bar")) {
            n.setStyle("-fx-bar-fill: #bc5090;");
        }

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
        ScrollPane pane = new ScrollPane(barChart);
        VBox vbox = new VBox(pane);
        Scene scene = new Scene(vbox, 600, 1200);

        Stage newWindow = new Stage();
        newWindow.setTitle("Gene Ontology: DGE vs. DAS Overenrichment");
        newWindow.setScene(scene);

        // Set position of second window, related to primary window.
        newWindow.setX(primaryStage.getX() + 200);
        newWindow.setY(primaryStage.getY() + 100);



        newWindow.show();
    }


}
