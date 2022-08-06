package org.jax.isopret.gui.controller;


import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.jax.isopret.visualization.Visualizable;
import org.jax.isopret.gui.service.HostServicesWrapper;
import org.jax.isopret.core.analysis.InterproOverrepResult;
import org.jax.isopret.gui.service.InterproAnnotatedGenesVisualizer;
import org.jax.isopret.gui.service.IsopretService;
import org.jax.isopret.gui.widgets.PopupFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

@Component
public class InterproController implements Initializable {
    private static final Logger LOGGER = LoggerFactory.getLogger(InterproController.class.getName());

    @FXML private TableView<InterproOverrepResult> interproResultTableView;
    @FXML private TextFlow interproTextFlow;
    @FXML private TableColumn<InterproOverrepResult, String> interproIdColumn;
    @FXML private TableColumn<InterproOverrepResult, String>  interproDescriptionColumn;
    @FXML private TableColumn<InterproOverrepResult, String> studyCountsColumn;
    @FXML private TableColumn<InterproOverrepResult, String>  populationCountsColumn;
    @FXML private TableColumn<InterproOverrepResult, String> rawPColumn;
    @FXML private TableColumn<InterproOverrepResult, String> adjPColumn;
    @FXML private TableColumn<InterproOverrepResult, Button>  exportColumn;



    @Autowired
    private MainController mainController;
    @Autowired
    private ResourceLoader resourceLoader;


    private final HostServicesWrapper hostServicesWrapper;

    private final List<InterproOverrepResult> interproResults;
    private final IsopretService isopretService;


    public InterproController(List<InterproOverrepResult> results, IsopretService service, HostServicesWrapper wrapper) {
        this.hostServicesWrapper = wrapper;
        this.interproResults = results;
        this.isopretService = service;
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
        // 5. population counts, e.g., 34/45
        populationCountsColumn.setSortable(false);
        populationCountsColumn.setEditable(false);
        populationCountsColumn.setCellValueFactory(cdf -> new ReadOnlyStringWrapper(cdf.getValue().getPopulationCounts()));
        // 7. raw p-value
        rawPColumn.setSortable(false);
        rawPColumn.setEditable(false);
        rawPColumn.setCellValueFactory(cdf -> new ReadOnlyStringWrapper(cdf.getValue().getRawP()));

        adjPColumn.setSortable(false);
        adjPColumn.setEditable(false);
        adjPColumn.setCellValueFactory(cdf -> new ReadOnlyStringWrapper(cdf.getValue().getBonferroniP()));


        if (hostServicesWrapper == null) {
            LOGGER.error("Could not retrieve HostServices");
        } else {
            exportColumn.setSortable(false);
            exportColumn.setEditable(false);
            exportColumn.setCellValueFactory(cdf -> {
                InterproOverrepResult interproResult = cdf.getValue();
                Button btn = new Button("Export");
                btn.setOnAction(e -> openExport(interproResult));
                // wrap it so it can be displayed in the TableView
                return new ReadOnlyObjectWrapper<>(btn);
            });
        }

        interproResultTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY); // do not show "extra column"

        Text text1 = new Text( "Interpro Overrepresentation Analysis\n");
        text1.setFont(Font.font("Verdana", 16));
        Text text2 = new Text("Interpro domain annotations were counted for each expressed transcript (population set). " +
               "Annotations for differentially spliced transcripts (study set) were counted. Overrepresentation " +
                "analysis was performed using a Fischer Exact Test. P-values were corrected using the Bonferroni procedure.");
        text2.setFont(Font.font("Verdana", 12));
        Text text3 = new Text("A total of " + interproResults.size() + " interpro domains were tested");
        text3.setFont(Font.font("Verdana", 12));
        this.interproTextFlow.getChildren().addAll(text1, text2, text3);

    }


        /**
         * This method is called to export an HTML file with SVGs for all genes
         * that are significant (either with respect to expression or splicing)
         * and are annotated to a certain Interpro term
         * @param interproResult The Interpro term (protein domain etc) of interest
         */
        private void openExport(InterproOverrepResult interproResult) {
            LOGGER.info("Exporting HTML for Interpro result {}", interproResult);
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Export " + interproResult.interproAccession());
            alert.setHeaderText("Export Information about " + interproResult.interproDescription());
            alert.setContentText(String.format("Export Information about differentially spliced genes annotated to %s",interproResult.interproAccession()));
            ButtonType buttonTypeOne = new ButtonType("Export");
            ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

            alert.getButtonTypes().setAll(buttonTypeOne, buttonTypeCancel);
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get().equals(buttonTypeOne)){
                InterproAnnotatedGenesVisualizer visualizer = new InterproAnnotatedGenesVisualizer(interproResult, isopretService);
                String html = visualizer.export();
                Optional<File> f = getDefaultFname("interpro", interproResult.interproAccession());
                if (f.isEmpty()) {
                    PopupFactory.displayError("Error", "Could not retrieve file.");
                    return;
                }
                try (BufferedWriter bw = new BufferedWriter(new FileWriter(f.get()))) {
                    bw.write(html);
                } catch (IOException e) {
                    PopupFactory.displayException("error", e.getMessage(), e);
                }
                alert.close();
            } else {
                alert.close();
            }
        }

    Optional<File> getDefaultFname(String type, String interproId) {
        Optional<File> hba = isopretService.getHbaDealsFileOpt();
        String hbaName = hba.map(File::getName).orElse("hbadeals");
        String fname = "isopret-" + type + "-" + interproId + "-" + hbaName + ".html";
        fname = fname.replaceAll(":", "_");
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("HTML files (*.html)", "*.html");
        fileChooser.getExtensionFilters().add(extFilter);
        fileChooser.setInitialFileName(fname);
        Stage stage = (Stage) this.interproTextFlow.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);
        return Optional.ofNullable(file);
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


    public void refreshTable() {
        javafx.application.Platform.runLater(() -> {
            LOGGER.trace("refreshTable: got a total of " + interproResults.size() + " interpro objects");
            interproResultTableView.getItems().clear(); /* clear previous rows, if any */
            interproResultTableView.getItems().addAll(interproResults);
            interproResultTableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        });
    }
}
