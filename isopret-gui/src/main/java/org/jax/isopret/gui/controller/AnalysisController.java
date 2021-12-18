package org.jax.isopret.gui.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import org.jax.isopret.core.hbadeals.HbaDealsResult;
import org.jax.isopret.gui.service.IsopretService;
import org.jax.isopret.gui.service.model.HbaDealsGeneRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AnalysisController implements Initializable {
    private static final Logger LOGGER = LoggerFactory.getLogger(AnalysisController.class.getName());

    /**
     * A map used to keep track of the open tabs. The Key is a reference to a viewpoint object, and the value is a
     * reference to a Tab that has been opened for it.
     */
    private final Map<HbaDealsResult, Tab> openTabs = new ConcurrentHashMap<>();

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
    private TableColumn<HbaDealsGeneRow, Button> actionTableColumn;
    @FXML
    private TableColumn<HbaDealsGeneRow, String> targetTableColumn;
    @FXML
    private TableColumn<HbaDealsGeneRow, String> genomicLocationColumn;
    @FXML
    private TableColumn<HbaDealsGeneRow, String> nSelectedTableColumn;
    @FXML
    private TableColumn<HbaDealsGeneRow, String> viewpointScoreColumn;
    @FXML
    private TableColumn<HbaDealsGeneRow, String> viewpointTotalLengthOfActiveSegments;
    @FXML
    private TableColumn<HbaDealsGeneRow, String> viewpointTotalLength;
    @FXML
    private TableColumn<HbaDealsGeneRow, String> fragmentOverlappingTSSColumn;
    @FXML
    private TableColumn<HbaDealsGeneRow, Button> deleteTableColumn;
    @FXML
    private TableColumn<HbaDealsGeneRow, Button> resetTableColumn;

    @FXML
    private TableColumn<HbaDealsGeneRow, String> manuallyRevisedColumn;



    /**
     * A reference to the main TabPane of the GUI. We will add new tabs to this that will show viewpoints in the
     * UCSC browser.
     */
    @FXML
    private TabPane tabPane;

    @Autowired
    private IsopretService isopretService;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }
}
