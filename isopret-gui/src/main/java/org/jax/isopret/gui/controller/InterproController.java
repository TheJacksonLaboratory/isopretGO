package org.jax.isopret.gui.controller;


import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import org.jax.isopret.core.visualization.Visualizable;
import org.jax.isopret.gui.service.HostServicesWrapper;
import org.jax.isopret.core.analysis.InterproOverrepResult;
import org.jax.isopret.gui.service.IsopretService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

@Component
public class InterproController implements Initializable {
    private static final Logger LOGGER = LoggerFactory.getLogger(InterproController.class.getName());

    @FXML private TableView<InterproOverrepResult> interproResultTableView;
    @FXML private TextFlow interproTextFlow;
    @FXML private TableColumn<InterproOverrepResult, String> interproIdColumn;
    @FXML private TableColumn<InterproOverrepResult, String>  interproDescriptionColumn;
    @FXML private TableColumn<InterproOverrepResult, String> studyCountsColumn;
    @FXML private TableColumn<InterproOverrepResult, String>  studyPercentageColumn;
    @FXML private TableColumn<InterproOverrepResult, String>  populationCountsColumn;
    @FXML private TableColumn<InterproOverrepResult, String>  populationPercentageColumn;
    @FXML private TableColumn<InterproOverrepResult, String> rawPColumn;
    @FXML private TableColumn<InterproOverrepResult, String> adjPColumn;


    @Autowired
    private IsopretService isopretService;
    @Autowired
    private MainController mainController;
    @Autowired
    private ResourceLoader resourceLoader;


    private final HostServicesWrapper hostServicesWrapper;

    private final List<InterproOverrepResult> interproResults;


    public InterproController(List<InterproOverrepResult> results, HostServicesWrapper wrapper) {
        this.hostServicesWrapper = wrapper;
        this.interproResults = results;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // 1. interpro accession, a String
        interproIdColumn.setSortable(true);
        interproIdColumn.setEditable(false);
        interproIdColumn.setCellValueFactory(cdf -> new ReadOnlyStringWrapper(cdf.getValue().interproAccession()));
        // 2. interpro description (name), a String
        interproDescriptionColumn.setSortable(false);
        interproDescriptionColumn.setEditable(false);
        interproDescriptionColumn.setCellValueFactory(cdf -> new ReadOnlyStringWrapper(cdf.getValue().interproDescription()));
        // 3. study counts, e.g., 34/45
        studyCountsColumn.setSortable(false);
        studyCountsColumn.setEditable(false);
        studyCountsColumn.setCellValueFactory(cdf -> new ReadOnlyStringWrapper(cdf.getValue().getStudyCounts()));
        // 4. study percentage,
        studyPercentageColumn.setSortable(false);
        studyPercentageColumn.setEditable(false);
        studyPercentageColumn.setCellValueFactory(cdf -> new ReadOnlyStringWrapper(cdf.getValue().getStudyPercentage()));
        // 5. population counts, e.g., 34/45
        populationCountsColumn.setSortable(false);
        populationCountsColumn.setEditable(false);
        populationCountsColumn.setCellValueFactory(cdf -> new ReadOnlyStringWrapper(cdf.getValue().getPopulationCounts()));
        // 6. population percentage,
        populationPercentageColumn.setSortable(false);
        populationPercentageColumn.setEditable(false);
        populationPercentageColumn.setCellValueFactory(cdf -> new ReadOnlyStringWrapper(cdf.getValue().getPopulationPercentage()));
        // 7. raw p-value
        rawPColumn.setSortable(false);
        rawPColumn.setEditable(false);
        rawPColumn.setCellValueFactory(cdf -> new ReadOnlyStringWrapper(cdf.getValue().getRawP()));

        adjPColumn.setSortable(false);
        adjPColumn.setEditable(false);
        adjPColumn.setCellValueFactory(cdf -> new ReadOnlyStringWrapper(cdf.getValue().getBonferroniP()));

        interproResultTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY); // do not show "extra column"

        Text text1 = new Text( "Interpro Overrepresentation Analysis\n");
        text1.setFont(Font.font("Verdana", 16));
        Text text2 = new Text(" Interpro domain annotations were counted for each expressed transcript (population set). " +
               "Annotations for differentially spliced transcripts (study set) were counted. Overrepresentation " +
                "analysis was performed using a Fischer Exact Test. P-values were corrected using the Bonferroni procedure.");
        text2.setFont(Font.font("Verdana", 12));
        Text text3 = new Text("A total of " + interproResults.size() + " interpro domains were tested");
        text3.setFont(Font.font("Verdana", 12));
        this.interproTextFlow.getChildren().addAll(text1, text2, text3);

    }


    /**
     * This is called when previous results are in the GUI and the user
     * starts a new analysis.
     */
    public void clearPreviousResults() {
        javafx.application.Platform.runLater(() -> {
            interproResultTableView.getItems().clear(); /* clear previous rows, if any */
        });
    }

    /**
     * Allow column name to be wrapped into multiple lines. Based on
     * <a href="https://stackoverflow.com/questions/10952111/javafx-2-0-table-with-multiline-table-header">this
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


    public void refreshTable() {
        javafx.application.Platform.runLater(() -> {
            LOGGER.trace("refreshTable: got a total of " + interproResults.size() + " interpro objects");
            interproResultTableView.getItems().clear(); /* clear previous rows, if any */
            interproResultTableView.getItems().addAll(interproResults);
            interproResultTableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        });
    }
}
