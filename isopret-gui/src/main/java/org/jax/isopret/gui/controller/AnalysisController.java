package org.jax.isopret.gui.controller;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.text.TextAlignment;
import org.jax.isopret.core.visualization.DoublePepValue;
import org.jax.isopret.core.visualization.Visualizable;
import org.jax.isopret.gui.service.HostServicesWrapper;
import org.jax.isopret.gui.service.IsopretService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.IOException;
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
    private final Map<Visualizable, Tab> openTabs = new ConcurrentHashMap<>();
    /** The search box for finding a given gene symbol in the rows of the table. */
    @FXML
    private TextField geneSymbolSearchBox;

    @FXML
    private ScrollPane VpAnalysisPane;

    @FXML
    private HBox listviewHbox;
    @FXML
    private ListView<String> lviewKey;
    @FXML
    private ListView<String> lviewValue;

    @FXML
    private TableView<Visualizable> hbaGeneResultTableView;
    @FXML
    private TableColumn<Visualizable, String> symbolColumn;
    @FXML
    private TableColumn<Visualizable, String> accessionColumn;
    @FXML
    private TableColumn<Visualizable, Double> foldChangeColumn;
    @FXML
    private TableColumn<Visualizable, DoublePepValue> genePepColumn;
    @FXML
    private TableColumn<Visualizable, String> isoformCountColumn;
    @FXML
    private TableColumn<Visualizable, DoublePepValue> isoformPepColumn;
    @FXML
    private TableColumn<Visualizable, Button> visualizeColumn;


    @Autowired
    private IsopretService isopretService;
    @Autowired @Lazy
    private MainController mainController;
    @Autowired
    private ResourceLoader resourceLoader;

    private final HostServicesWrapper hostServicesWrapper;

    public AnalysisController(HostServicesWrapper wrapper) {
        this.hostServicesWrapper = wrapper;
    }

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

        genePepColumn.setSortable(true);
        genePepColumn.setEditable(false);
        genePepColumn.setCellValueFactory(cdf ->
                new ReadOnlyObjectWrapper<>(cdf.getValue().getExpressionPepValue()));
        genePepColumn.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(DoublePepValue dpep, boolean empty) {
                super.updateItem(dpep, empty);
                if (dpep == null || empty) {
                    setText(null);
                } else {
                    setText(scientificNotation(dpep.pep()));
                    if (dpep.isSignificant()) {
                        String color = getColorFromExpressionPep(dpep);
                        setStyle("-fx-background-color: " + color);
                    } else {
                        setStyle("-fx-background-color: white");
                    }
                }
            }
        });

        isoformCountColumn.setSortable(false);
        isoformCountColumn.setEditable(false);
        isoformCountColumn.setCellValueFactory(cdf -> new ReadOnlyStringWrapper(cdf.getValue().getNofMsplicing()));


        isoformPepColumn.setSortable(true);
        isoformPepColumn.setEditable(false);
        isoformPepColumn.setCellValueFactory(cdf -> new ReadOnlyObjectWrapper<>(cdf.getValue().getSplicingPepValue()));
        isoformPepColumn.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(DoublePepValue dpep, boolean empty) {
                super.updateItem(dpep, empty);
                if (dpep == null || empty) {
                    setText(null);
                } else {
                    setText(scientificNotation(dpep.pep()));
                    if (dpep.isSignificant()) {
                        String color = getColorFromSplicingPep(dpep);
                        setStyle("-fx-background-color: " + color);
                    } else {
                        setStyle("-fx-background-color: white");
                    }
                }
            }
        });


        visualizeColumn.setSortable(false);
        visualizeColumn.setCellValueFactory(cdf -> {
            Visualizable geneRow = cdf.getValue();
            Button btn = new Button("Visualize");
            btn.setOnAction(e -> {
                LOGGER.trace(String.format("Adding tab for row with generow: %s", geneRow.getGeneSymbol()));
                openHbaDealsResultInTab(geneRow);
            });
            // wrap it so it can be displayed in the TableView
            return new ReadOnlyObjectWrapper<>(btn);
        });
        // allow titles of all table columns to be broken into multiple lines
        hbaGeneResultTableView.getColumns().forEach(AnalysisController::makeHeaderWrappable);
        hbaGeneResultTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY); // do not show "extra column"

    }

    static String getColorFromExpressionPep(DoublePepValue d) {
        // from medium intense to very light
        String [] greens = { "#48c9b0", "#76d7c4", "#a3e4d7", "#d1f2eb", "#e8f8f5" };
        String [] yellows = {"#f4d03f", "#f7dc6f", "#f9e79f", "#fcf3cf", "#fef9e7" };
        if (d.pep() == 0.0) return greens[0];
        else if (d.pep() < 1e-6) return greens[1];
        else if (d.pep() < 1e-3) return greens[2];
        else if (d.pep() <= 0.05) return greens[3];
        else if (d.isSignificant()) return greens[4];
        else return "white";
    }

    static String getColorFromSplicingPep(DoublePepValue d) {
        // from medium intense to very light
        String [] yellows = {"#f4d03f", "#f7dc6f", "#f9e79f", "#fcf3cf", "#fef9e7" };
        if (d.pep() == 0.0) return yellows[0];
        else if (d.pep() < 1e-6) return yellows[1];
        else if (d.pep() < 1e-3) return yellows[2];
        else if (d.pep() <= 0.05) return yellows[3];
        else if (d.isSignificant()) return yellows[4];
        else return "white";
    }



    public String scientificNotation(Double d) {
        Formatter fmt = new Formatter();
        if (d == null) return "1.0";
        else if (d > 0.01) return String.format("%.3f", d);
        else if (d > 0.001) return String.format("%.4f", d);
        else if (d == 0.0) return "0";
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
            List<Visualizable> vpl = this.isopretService.getGeneVisualizables();
            LOGGER.trace("refreshVPTable: got a total of " + vpl.size() + " ViewPoint objects");
            hbaGeneResultTableView.getItems().clear(); /* clear previous rows, if any */
            hbaGeneResultTableView.getItems().addAll(vpl);
            hbaGeneResultTableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
            AnchorPane.setTopAnchor(hbaGeneResultTableView, listviewHbox.getLayoutY() + listviewHbox.getHeight());
            hbaGeneResultTableView.sort();
            // set up the search bar
            geneSymbolSearchBox.textProperty().addListener((observable, oldValue, newValue) ->
                    hbaGeneResultTableView.setItems(filterVisualizableList(vpl, newValue))
            );
        });
    }

    /**
     * This is called when previous results are in the GUI and the user
     * starts a new analysis.
     */
    public void clearPreviousResults() {
        javafx.application.Platform.runLater(() -> {
            hbaGeneResultTableView.getItems().clear(); /* clear previous rows, if any */
            lviewKey.getItems().clear();
            lviewValue.getItems().clear();
        });
    }


    /**
     * This method creates a new {@link Tab} populated with a viewpoint!
     *
     * @param hbadealsResult This {@link Visualizable} object will be opened into a new Tab.
     */
    private void openHbaDealsResultInTab(Visualizable hbadealsResult) {
        TabPane tabPane = this.mainController.getMainTabPaneRef();
        /* First check if we have already opened a tab for this gene. */
        if (openTabs.containsKey(hbadealsResult)) {
            Tab tab = openTabs.get(hbadealsResult);
            LOGGER.trace("openTabs already containsKey " + hbadealsResult.getGeneSymbol());
            if (tab == null || tab.isDisabled()) {
                LOGGER.trace("Tab is null (error), REMOVING " + hbadealsResult.getGeneSymbol());
                openTabs.remove(hbadealsResult);
            } else {
                LOGGER.trace("openTabs SELECTING " + hbadealsResult.getGeneSymbol());
                tabPane.getSelectionModel().select(tab);
                return;
            }
        }
        // If we get here, there is no tab yet for this gene
        LOGGER.trace("openTabs with new tab for" + hbadealsResult.getGeneSymbol());

        final Tab tab = new Tab(hbadealsResult.getGeneSymbol());
        tab.setId(hbadealsResult.getGeneSymbol());
        tab.setClosable(true);
        tab.setOnClosed(event -> {
            if (tabPane.getTabs()
                    .size() == 2) {
                event.consume();
            }
        });

        tab.setOnCloseRequest((e)-> {
            for (Visualizable vpnt : this.openTabs.keySet()) {
                Tab t = this.openTabs.get(vpnt);
                if (t.equals(tab)) {
                    this.openTabs.remove(vpnt);
                }
            }
        });
        try {
            Resource r = resourceLoader.getResource(
                    "classpath:fxml/hbaGenePane.fxml");
            if (! r.exists()) {
                LOGGER.error("Could not initialize hbaGenePane pane (fxml file not found)");
                return;
            }
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(r.getURL()));
            loader.setControllerFactory(c -> new HbaGeneController(hbadealsResult, this.isopretService, this.hostServicesWrapper));
            ScrollPane p = loader.load();
            HbaGeneController hbaGeneController = loader.getController();
            hbaGeneController.refreshTables();
            tab.setContent(p);
            tabPane.getTabs().add(tab);
            tabPane.getSelectionModel().select(tab);
            this.openTabs.put(hbadealsResult, tab);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }


    /**
     * Allow column name to be wrapped into multiple lines. Based on
     * <a href="<a href="https://stackoverflow.com/questions/10952111/javafx-2-0-table-with-multiline-table-header">https://stackoverflow.com/questions/10952111/javafx-2-0-table-with-multiline-table-header</a>">this
     * post</a>.
     *
     * @param col {@link TableColumn} with a name that will be wrapped
     */
    public static  <T> void makeHeaderWrappable(TableColumn<Visualizable, T> col) {
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

    /**
     * Used to search the rows of hte analysis table by matching on the gene symbol
     * @param visualizable A {@link Visualizable} object in our Table row
     * @param searchText text representing part or all of a gene symbol
     * @return true if the gene symbol in the visualizable matches the search text
     */
    private boolean searchGeneSymbol(Visualizable visualizable, String searchText){
        return visualizable.getGeneSymbol().toLowerCase().contains(searchText.toLowerCase());
    }

    private ObservableList<Visualizable> filterVisualizableList(List<Visualizable> list, String searchText){
        List<Visualizable> filteredList = new ArrayList<>();
        for (Visualizable visualizable : list){
            if(searchGeneSymbol(visualizable, searchText)) filteredList.add(visualizable);
        }
        return FXCollections.observableList(filteredList);
    }

    /**
     * This method is called when the user clicks the "delete" button (X) to the right of the
     * gene symbol search field.
     */
    public void handleClearSearchText(ActionEvent e) {
        geneSymbolSearchBox.setText("");
        e.consume();
    }

}
