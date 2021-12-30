package org.jax.isopret.gui.controller;

import javafx.application.HostServices;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.jax.isopret.core.visualization.IsoformVisualizable;
import org.jax.isopret.core.visualization.Visualizable;
import org.jax.isopret.gui.service.IsopretService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private IsopretService service;


    private final Visualizable result;

    private final String html;


    public HbaGeneController(Visualizable hbadealsResult, String html) {
        result = hbadealsResult;
        this.html = html;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.hbaGeneLabel.setText(result.getGeneSymbol());
        String geneAccession = result.getGeneAccession();
        geneHyperlink.setText(geneAccession);
        geneHyperlink.setOnAction(e -> {
            String address = "https://www.ensembl.org/Homo_sapiens/Gene/Summary?db=core;g=" + geneAccession;
            HostServices hostServices = service.getHostServices();
            if (hostServices != null) {
                hostServices.showDocument(address);
            } else {
                LOGGER.error("Could not get reference to host services");
            }
            e.consume();
        });
        String fc = String.format("Gene expression fold-change: %.2f",result.getExpressionFoldChange());
        geneFoldChangeLabel.setText(fc);
        String prob = String.format("Probability (PEP): %.2f", result.getExpressionPval());
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

        isoformTableView.getItems().clear();
        isoformTableView.getItems().addAll(result.getIsoformVisualizable());
        isoformTableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        WebEngine webEngine = hbaGeneWebView.getEngine();
        webEngine.loadContent(this.html);
    }
}
