package org.jax.isopret.gui.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.jax.isopret.gui.service.model.HbaDealsGeneRow;
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
    private VBox hbaGeneVbox;
    @FXML
    private WebView hbaGeneWebView;


    private final HbaDealsGeneRow result;

    private final String html;


    public HbaGeneController(HbaDealsGeneRow hbadealsResult, String html) {
        result = hbadealsResult;
        this.html = html;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        WebEngine webEngine = hbaGeneWebView.getEngine();
        webEngine.loadContent(this.html);
    }
}
