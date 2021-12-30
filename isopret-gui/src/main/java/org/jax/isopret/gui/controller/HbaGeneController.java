package org.jax.isopret.gui.controller;

import javafx.application.HostServices;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
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
        WebEngine webEngine = hbaGeneWebView.getEngine();
        webEngine.loadContent(this.html);
    }
}
