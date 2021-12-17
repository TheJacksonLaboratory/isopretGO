package org.jax.isopret.gui.controller;


import com.google.j2objc.annotations.AutoreleasePool;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.BorderPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.jax.isopret.gui.service.IsopretService;
import org.jax.isopret.gui.widgets.PopupFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;

/**
 * A Java app to help design probes for Capture Hi-C
 * @author Peter Robinson
 * @version 0.0.1 (2021-11-27)
 */
@Component
public class MainController implements Initializable {
    private final static Logger LOGGER = LoggerFactory.getLogger(MainController.class.getName());

    @FXML
    private BorderPane rootNode;
    @FXML
    private Label downloadDataSourceLabel;
    @FXML
    private ProgressIndicator transcriptDownloadPI;

    @FXML
    private final ObservableList<String> goMethodList = FXCollections.observableArrayList("Term for Term",
            "Parent-Child Union", "Parent-Child Intersect");
    @FXML
    private ChoiceBox<String> goChoiceBox;

    @Autowired
    private IsopretService service;

    @Autowired
    private Properties pgProperties;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Bindings.bindBidirectional(this.downloadDataSourceLabel.textProperty(), service.downloadDirProperty());
        this.transcriptDownloadPI.progressProperty().bind(service.downloadCompletenessProperty());
        goChoiceBox.setItems(goMethodList);
        goChoiceBox.getSelectionModel().selectFirst();
        goChoiceBox.valueProperty().addListener((observable, oldValue, newValue) -> service.setGoMethod(newValue) );
    }




    @FXML
    private void chooseHbaDealsOutputFile(ActionEvent e) {
        e.consume();
        FileChooser chooser = new FileChooser();
        chooser.setInitialDirectory(new File(System.getProperty("user.home")));
        chooser.setTitle("Choose HBA-DEALS File");
        File file = chooser.showOpenDialog(rootNode.getScene().getWindow());
        if (file==null || file.getAbsolutePath().equals("")) {
            LOGGER.error("Could not get HBA-DEALS file");
            PopupFactory.displayError("Error","Could not get HBA-DEALS file.");
            return;
        }
        service.setHbaDealsFile(file);
    }

    @FXML
    private void downloadSources(ActionEvent e) {
        e.consume();
        if (service.sourcesDownloaded()) {
            LOGGER.info("Sources previously downloaded");
        }
        DirectoryChooser dirChooser = new DirectoryChooser();
        dirChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        dirChooser.setTitle("Choose directory for downloading files required for isopret.");
        File file = dirChooser.showDialog(rootNode.getScene().getWindow());
        if (file==null || file.getAbsolutePath().equals("")) {
            LOGGER.error("Could not get directory for download");
            PopupFactory.displayError("Error","Could not get directory for download.");
            return;
        }
        service.downloadSources(file);
    }


    @FXML
    private void about(ActionEvent e) {
        String version = "TODO";
        String lastChangedDate = "TODO";
        PopupFactory.showAbout(version, lastChangedDate);
        e.consume();
    }

    /**
     * Write the settings from the current session to file and exit.
     */
    @FXML
    private void exitGui() {
        javafx.application.Platform.exit();
    }
}
