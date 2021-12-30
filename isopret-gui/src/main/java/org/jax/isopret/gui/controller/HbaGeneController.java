package org.jax.isopret.gui.controller;

import javafx.application.HostServices;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.jax.isopret.core.visualization.InterproVisualizable;
import org.jax.isopret.core.visualization.IsoformVisualizable;
import org.jax.isopret.core.visualization.Visualizable;
import org.jax.isopret.gui.service.IsopretService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.ResourceBundle;

@Component
@Scope("prototype")
public class HbaGeneController implements Initializable {
    private static final Logger LOGGER = LoggerFactory.getLogger(HbaGeneController.class.getName());


    @FXML
    private TableView<IsoformVisualizable> isoformTableView;
    @FXML
    private TableColumn<IsoformVisualizable, String> accessionColumn;
    @FXML
    private TableColumn<IsoformVisualizable, String> urlColumn;
    @FXML
    private TableColumn<IsoformVisualizable, String> isoformLogFcColumn;
    @FXML
    private TableColumn<IsoformVisualizable, String> isoformPColumn;

    @FXML
    private  TableView<InterproVisualizable> interprTableView;
    @FXML
    private TableColumn<InterproVisualizable, String> interproAccessionColumn;
    @FXML
    private TableColumn<InterproVisualizable, String> interproEntryType;
    @FXML
    private TableColumn<InterproVisualizable, String> interproDescription;

    @FXML
    private Label hbaGeneLabel;
    @FXML
    private Hyperlink geneHyperlink;
    @FXML
    private Label geneFoldChangeLabel;
    @FXML
    private  Label geneProbabilityLabel;
    @FXML
    private VBox hbaGeneVbox;
    @FXML
    private WebView hbaGeneWebView;
    @FXML
    private WebView hbaProteinWebView;

    private final IsopretService service;

    private final Visualizable visualizable;

    public HbaGeneController(Visualizable visualisable, IsopretService isoservice) {
        this.visualizable = visualisable;
        this.service = isoservice;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.hbaGeneLabel.setText(visualizable.getGeneSymbol());
        String geneAccession = visualizable.getGeneAccession();
        geneHyperlink.setText(geneAccession);

        String fc = String.format("Gene expression fold-change: %.2f", visualizable.getExpressionFoldChange());
        geneFoldChangeLabel.setText(fc);
        String prob = String.format("Probability (PEP): %.2f", visualizable.getExpressionPval());
        this.geneProbabilityLabel.setText(prob);
        // isoform table
        accessionColumn.setSortable(false);
        accessionColumn.setEditable(false);
        accessionColumn.setCellValueFactory(v ->  new ReadOnlyStringWrapper(v.getValue().transcriptAccession()));
        urlColumn.setEditable(false);
        urlColumn.setSortable(false);
        urlColumn.setCellValueFactory(v -> new ReadOnlyStringWrapper("todo"));
        isoformLogFcColumn.setSortable(false);
        isoformLogFcColumn.setEditable(false);
        isoformLogFcColumn.setCellValueFactory(v -> new ReadOnlyStringWrapper(v.getValue().log2Foldchange()));
        isoformPColumn.setSortable(false);
        isoformPColumn.setEditable(false);
        isoformPColumn.setCellValueFactory(v -> new ReadOnlyStringWrapper(v.getValue().isoformP()));
        isoformTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY); // do not show "extra column"
        LOGGER.error("Adding isoform vis n={} items", visualizable.getIsoformVisualizable().size());
        WebEngine webEngine = hbaGeneWebView.getEngine();
        webEngine.loadContent(this.visualizable.getIsoformHtml());
        // interpro table
        interproAccessionColumn.setEditable(false);
        interproAccessionColumn.setSortable(false);
        interproAccessionColumn.setCellValueFactory(v -> new ReadOnlyStringWrapper(v.getValue().getInterproAccession()));
        interproEntryType.setEditable(false);
        interproEntryType.setSortable(false);
        interproEntryType.setCellValueFactory(v -> new ReadOnlyStringWrapper(v.getValue().getEntryType()));
        interproDescription.setEditable(false);
        interproDescription.setSortable(false);
        interproDescription.setCellValueFactory(v -> new ReadOnlyStringWrapper(v.getValue().getDescription()));
        interprTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY); // do not show "extra column"
        WebEngine interprebEngine = hbaProteinWebView.getEngine();
        interprebEngine.loadContent(this.visualizable.getProteinHtml());
    }

    /**
     * Add content to the tables.
     */
    public void refreshTables() {
        geneHyperlink.setOnAction(e -> {
            String geneAccession = visualizable.getGeneAccession();
            String address = "https://www.ensembl.org/Homo_sapiens/Gene/Summary?db=core;g=" + geneAccession;
            HostServices hostServices = service.getHostServices();
            if (hostServices == null) {
                LOGGER.info("TRY AGAIN");
                hostServices = (HostServices) this.isoformTableView.getProperties().get("hostServices");
            }
            if (hostServices != null) {
                hostServices.showDocument(address);
            } else {
                LOGGER.error("Could not get reference to host services");
            }
            e.consume();
        });
        javafx.application.Platform.runLater(() -> {
            isoformTableView.getItems().clear();
            isoformTableView.getItems().addAll(visualizable.getIsoformVisualizable());
            isoformTableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
            isoformTableView.setFixedCellSize(25);
            isoformTableView.prefHeightProperty().bind(Bindings.size(isoformTableView.getItems()).multiply(isoformTableView.getFixedCellSize()).add(40));
            hbaGeneWebView.setMaxHeight(visualizable.getIsoformSvgHeight());
            LOGGER.info("Loading isoform table");
            interprTableView.getItems().clear();
            interprTableView.getItems().addAll(visualizable.getInterproVisualizable());
            interprTableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
            interprTableView.setFixedCellSize(25);
            interprTableView.prefHeightProperty().bind(Bindings.size(interprTableView.getItems()).multiply(isoformTableView.getFixedCellSize()).add(40));
            LOGGER.info("Loading isoform table");
        });
    }
}
