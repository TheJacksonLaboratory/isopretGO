package org.jax.isopret.gui.controller;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.text.TextAlignment;
import org.jax.isopret.gui.service.IsopretService;
import org.jax.isopret.gui.service.model.HbaDealsGeneRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AnalysisController implements Initializable {
    private static final Logger LOGGER = LoggerFactory.getLogger(AnalysisController.class.getName());

    /**
     * A map used to keep track of the open tabs. The Key is a reference to a viewpoint object, and the value is a
     * reference to a Tab that has been opened for it.
     */
    private final Map<HbaDealsGeneRow, Tab> openTabs = new ConcurrentHashMap<>();

    @FXML
    private ScrollPane VpAnalysisPane;

    @FXML
    private HBox listviewHbox;
    @FXML
    private ListView<String> lviewKey;
    @FXML
    private ListView<String> lviewValue;

    @FXML
    private TableView<HbaDealsGeneRow> viewPointTableView;
    @FXML
    private TableColumn<HbaDealsGeneRow, String> symbolColumn;
    @FXML
    private TableColumn<HbaDealsGeneRow, String> accessionColumn;
    @FXML
    private TableColumn<HbaDealsGeneRow, Double> foldChangeColumn;
    @FXML
    private TableColumn<HbaDealsGeneRow, Double> geneProbabilityColumn;
    @FXML
    private TableColumn<HbaDealsGeneRow, String> isoformCountColumn;
    @FXML
    private TableColumn<HbaDealsGeneRow, Double> isoformProbabilityColumn;
    @FXML
    private TableColumn<HbaDealsGeneRow, Button> visualizeColumn;


    @Autowired
    private IsopretService isopretService;
    @Autowired
    private MainController mainController;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // 1. the gene symbol
        symbolColumn.setSortable(true);
        symbolColumn.setEditable(false);
        symbolColumn.setCellValueFactory(cdf -> new ReadOnlyStringWrapper(cdf.getValue().getGeneSymbol()));
        // 2. the ENSG accession number
        accessionColumn.setSortable(true);
        accessionColumn.setEditable(false);
        accessionColumn.setCellValueFactory(cdf -> new ReadOnlyStringWrapper(cdf.getValue().getGeneAccession()));

        foldChangeColumn.setSortable(true);
        foldChangeColumn.setEditable(false);
        foldChangeColumn.setCellValueFactory(cdf -> new ReadOnlyObjectWrapper<>(cdf.getValue().getExpressionFoldChange()));
        foldChangeColumn.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(Double balance, boolean empty) {
                super.updateItem(balance, empty);
                if (balance == null || empty) {
                    setText(null);
                } else {
                    setText(String.format("%.2f", balance));
                }
            }
        });

        geneProbabilityColumn.setSortable(true);
        geneProbabilityColumn.setEditable(false);
        geneProbabilityColumn.setCellValueFactory(cdf -> new ReadOnlyObjectWrapper<>(cdf.getValue().getExpressionPval()));
        geneProbabilityColumn.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(Double balance, boolean empty) {
                super.updateItem(balance, empty);
                if (balance == null || empty) {
                    setText(null);
                } else {
                    setText(scientificNotation(balance));
                }
            }
        });

        isoformCountColumn.setSortable(false);
        isoformCountColumn.setEditable(false);
        isoformCountColumn.setCellValueFactory(cdf -> new ReadOnlyStringWrapper(cdf.getValue().getNofMsplicing()));


        isoformProbabilityColumn.setSortable(true);
        isoformProbabilityColumn.setEditable(false);
        isoformProbabilityColumn.setCellValueFactory(cdf -> new ReadOnlyObjectWrapper<>(cdf.getValue().getBestSplicingPval()));
        isoformProbabilityColumn.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(Double balance, boolean empty) {
                super.updateItem(balance, empty);
                if (balance == null || empty) {
                    setText(null);
                } else {
                    setText(scientificNotation(balance));
                }
            }
        });


        visualizeColumn.setSortable(false);
        visualizeColumn.setCellValueFactory(cdf -> {
            HbaDealsGeneRow geneRow = cdf.getValue();
            Button btn = new Button("Visualize");
            btn.setOnAction(e -> {
                LOGGER.trace(String.format("Adding tab for row with generow: %s", geneRow.getGeneSymbol()));
                openViewPointInTab(geneRow);
            });
            // wrap it so it can be displayed in the TableView
            return new ReadOnlyObjectWrapper<>(btn);
        });
        // allow titles of all table columns to be broken into multiple lines
        viewPointTableView.getColumns().forEach(AnalysisController::makeHeaderWrappable);
    }


    public String scientificNotation(Double d) {
        Formatter fmt = new Formatter();
        if (d == null) return "1.0";
        else if (d > 0.01) return String.format("%.3f", d);
        else if (d > 0.001) return String.format("%.4f", d);
        else return fmt.format("%16.2e",d).toString();
    }


    public void refreshListView() {
        if (isopretService == null) {
            LOGGER.error("isopretService null--should never happen");
            return;
        }
        Map<String, String> summaryMap = isopretService.getResultsSummaryMap();

        ObservableList<String> keys = FXCollections.observableArrayList(summaryMap.keySet());
        ObservableList<String> values = FXCollections.observableArrayList(summaryMap.values());
        lviewKey.setItems(keys);
        lviewValue.setItems(values);
    }

    /**
     * This method is called to refresh the values of the ViewPoint in the table of the analysis tab.
     */
    public void refreshVPTable() {
        if (isopretService == null) {
            LOGGER.error("isopretService null--should never happen");
            return;
        }
        javafx.application.Platform.runLater(() -> {
            List<HbaDealsGeneRow> vpl = this.isopretService.getHbaDealsRows();
            LOGGER.trace("refreshVPTable: got a total of " + vpl.size() + " ViewPoint objects");
            viewPointTableView.getItems().clear(); /* clear previous rows, if any */
            viewPointTableView.getItems().addAll(vpl);
            viewPointTableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
            AnchorPane.setTopAnchor(viewPointTableView, listviewHbox.getLayoutY() + listviewHbox.getHeight());
            viewPointTableView.sort();
        });
    }


    /**
     * This method creates a new {@link Tab} populated with a viewpoint!
     *
     * @param vp This {@link HbaDealsGeneRow} object will be opened into a new Tab.
     */
    private void openViewPointInTab(HbaDealsGeneRow vp) {
        TabPane tabPane = this.mainController.getMainTabPaneRef();
        /* First check if we have already opened a tab for this gene. */
        if (openTabs.containsKey(vp)) {
            Tab tab = openTabs.get(vp);
            LOGGER.trace("openTabs already containsKey " + vp.getGeneSymbol());
            if (tab == null || tab.isDisabled()) {
                LOGGER.trace("Tab is null (error), REMOVING " + vp.getGeneSymbol());
                openTabs.remove(vp);
            } else {
                LOGGER.trace("openTabs SELECTING " + vp.getGeneSymbol());
                tabPane.getSelectionModel().select(tab);
                return;
            }
        }
        // If we get here, there is no tab yet for this gene
        LOGGER.trace("openTabs with new tab for" + vp.getGeneSymbol());

        final Tab tab = new Tab("Viewpoint: " + vp.getGeneSymbol());
        tab.setId(vp.getGeneSymbol());
        tab.setClosable(true);
        tab.setOnClosed(event -> {
            if (tabPane.getTabs()
                    .size() == 2) {
                event.consume();
            }
        });

        tab.setOnCloseRequest((e)-> {
            for (HbaDealsGeneRow vpnt : this.openTabs.keySet()) {
                Tab t = this.openTabs.get(vpnt);
                if (t.equals(tab)) {
                    this.openTabs.remove(vpnt);
                }
            }
        });

        tabPane.getTabs().add(tab);
        tabPane.getSelectionModel().select(tab);
        this.openTabs.put(vp, tab);
    }


    /**
     * Allow column name to be wrapped into multiple lines. Based on
     * <a href="https://stackoverflow.com/questions/10952111/javafx-2-0-table-with-multiline-table-header">this
     * post</a>.
     *
     * @param col {@link TableColumn} with a name that will be wrapped
     */
    public static  <T> void makeHeaderWrappable(TableColumn<HbaDealsGeneRow, T> col) {
        Label label = new Label(col.getText());
        label.setStyle("-fx-padding: 8px;");
        label.setWrapText(true);
        label.setAlignment(Pos.CENTER);
        label.setTextAlignment(TextAlignment.CENTER);
        StackPane stack = new StackPane();
        stack.getChildren().add(label);
        stack.prefWidthProperty().bind(col.widthProperty().subtract(5));
        label.prefWidthProperty().bind(stack.prefWidthProperty());
        col.setGraphic(stack);
    }

}

/*
private void createTabDynamically() {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("secondView.fxml"));
        loader.setController(new SecondViewController());
        try {
            Parent parent = loader.load();
            myDynamicTab = new Tab("A Dynamic Tab");
            myDynamicTab.setClosable(true);
            myDynamicTab.setContent(parent);
            tabPane.getTabs().add(myDynamicTab);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

SecondViewController.java

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

public class SecondViewController implements Initializable {

    @FXML private Label secondInfoLbl;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        secondInfoLbl.setText("Hello from the second view");
    }
}

 */