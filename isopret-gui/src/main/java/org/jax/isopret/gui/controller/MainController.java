package org.jax.isopret.gui.controller;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import org.jax.isopret.gui.widgets.PopupFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * A Java app to help design probes for Capture Hi-C
 * @author Peter Robinson
 * @version 0.0.1 (2021-11-27)
 */
@Component
public class MainController implements Initializable {
    private final static Logger LOGGER = LoggerFactory.getLogger(MainController.class.getName());


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }



    @FXML
    private void about(ActionEvent e) {
        String version = "TODO";
        String lastChangedDate = "TODO";
        PopupFactory.showAbout(version, lastChangedDate);
        e.consume();
    }
}
