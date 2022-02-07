package org.jax.isopret.gui.controller;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.jax.isopret.gui.service.GoHtmlExporter;
import org.jax.isopret.gui.service.HostServicesWrapper;
import org.jax.isopret.gui.service.IsopretService;
import org.jax.isopret.gui.service.model.GeneOntologyComparisonMode;
import org.jax.isopret.gui.service.model.GoComparison;
import org.jax.isopret.gui.service.model.GoTermAndPvalVisualized;
import org.jax.isopret.gui.widgets.GoCompWidget;
import org.jax.isopret.gui.widgets.GoDisplayWidget;
import org.jax.isopret.gui.widgets.PopupFactory;
import org.monarchinitiative.phenol.analysis.stats.GoTerm2PValAndCounts;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Controller that is used to display the DGE or DAS data.
 * The class is used for both DGE and DAS tabs, which is why it is given prototype scope
 * @author Peter Robinson
 */
@Component
@Scope("prototype")
public class GeneOntologyController implements Initializable {
    private static final Logger LOGGER = LoggerFactory.getLogger(GeneOntologyController.class.getName());


    private final List<GoTermAndPvalVisualized> goPvals;
    /* Main pane of the GO Overpresentation tabs */
    public ScrollPane geneOntologyPane;
    public TableView<GoTermAndPvalVisualized> goPvalTableView;
    public TableColumn<GoTermAndPvalVisualized, String> termColumn;
    public TableColumn<GoTermAndPvalVisualized, Button> termIdColumn;
    public TableColumn<GoTermAndPvalVisualized, String> studyCountsColumn;
    public TableColumn<GoTermAndPvalVisualized, String> populationCountsColumn;
    public TableColumn<GoTermAndPvalVisualized, String> pvalColumn;
    public TableColumn<GoTermAndPvalVisualized, String> adjpvalColumn;
    public TableColumn<GoTermAndPvalVisualized, Button> exportColumn;
    private final String label;
    private final GeneOntologyComparisonMode comparisonMode;
    private final String methodsLabel;
    private final String summaryLabel;
    public Label goTopLevelLabel;
    public Label goMethodsLabel;
    public Label goSummaryLabel;

    private final IsopretService isopretService;

    private final HostServicesWrapper hostServices;
    @FXML
    private Button dgeOrDasGoBtn;


    public GeneOntologyController(GeneOntologyComparisonMode mode, List<GoTerm2PValAndCounts> pvals, IsopretService service, HostServicesWrapper hostServicesWrapper) {
        this.label = service.getGoLabel(mode);
        comparisonMode = mode;
        this.methodsLabel = service.getGoMethods();
        this.summaryLabel = service.getGoSummary();
        this.isopretService = service;
        this.hostServices = hostServicesWrapper;
        this.goPvals = pvals.stream()
                .map(pval -> new GoTermAndPvalVisualized(pval, service.getGeneOntology()))
                .collect(Collectors.toList());
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        termColumn.setSortable(false);
        termColumn.setEditable(false);
        termColumn.setCellValueFactory(cdf -> new ReadOnlyStringWrapper(cdf.getValue().getGoTermLabel()));

        termIdColumn.setSortable(false);
        termIdColumn.setEditable(false);
        if (hostServices == null) {
            termIdColumn.setCellValueFactory(cdf -> {
                LOGGER.error("Could not retrieve HostServices");
                GoTermAndPvalVisualized geneRow = cdf.getValue();
                String termId = geneRow.getGoTermId();
                Button btn = new Button(termId);
                return new ReadOnlyObjectWrapper<>(btn);
            });
            } else {
            termIdColumn.setCellValueFactory(cdf -> {
                GoTermAndPvalVisualized geneRow = cdf.getValue();
                String termId = geneRow.getGoTermId();
                String amigoUrl = "http://amigo.geneontology.org/amigo/term/" + termId;
                Button btn = new Button(termId);
                btn.setOnAction(e -> { hostServices.showDocument(amigoUrl);
                    LOGGER.trace(String.format("Calling URL: %s", amigoUrl));
                });
                // wrap it so it can be displayed in the TableView
                return new ReadOnlyObjectWrapper<>(btn);
            });
        }
        studyCountsColumn.setSortable(false);
        studyCountsColumn.setEditable(false);
        studyCountsColumn.setCellValueFactory(cdf -> new ReadOnlyStringWrapper(cdf.getValue().getStudyGeneRatio()));

        populationCountsColumn.setSortable(false);
        populationCountsColumn.setEditable(false);
        populationCountsColumn.setCellValueFactory(cdf -> new ReadOnlyStringWrapper(cdf.getValue().getPopulationGeneRatio()));

        pvalColumn.setSortable(false);
        pvalColumn.setEditable(false);
        pvalColumn.setCellValueFactory(cdf -> new ReadOnlyStringWrapper(cdf.getValue().getPvalFormated()));

        adjpvalColumn.setSortable(false);
        adjpvalColumn.setEditable(false);
        adjpvalColumn.setCellValueFactory(cdf -> new ReadOnlyStringWrapper(cdf.getValue().getPvalAdjFormated()));

        if (hostServices == null) {
            LOGGER.error("Could not retrieve HostServices");
        } else {
            exportColumn.setSortable(false);
            exportColumn.setEditable(false);
            exportColumn.setCellValueFactory(cdf -> {
                GoTermAndPvalVisualized geneRow = cdf.getValue();
                String termId = geneRow.getGoTermId();
                TermId tid = TermId.of(termId);
                Button btn = new Button("Export");
                btn.setOnAction(e -> openExport(tid));
                // wrap it so it can be displayed in the TableView
                return new ReadOnlyObjectWrapper<>(btn);
            });
        }

        goPvalTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY); // do not show "extra column"
        this.goTopLevelLabel.setText(this.label);
        this.goMethodsLabel.setText(this.methodsLabel);
        this.goSummaryLabel.setText(this.summaryLabel);
        if (this.comparisonMode.equals(GeneOntologyComparisonMode.DGE)) {
            this.dgeOrDasGoBtn.setText("GO Enrichment (DGE)");
        } else {
            this.dgeOrDasGoBtn.setText("GO Enrichment (DAS)");
        }
    }


    /**
     * This method is called to to add the GO overrepresentation data to the table.
     */
    public void refreshGeneOntologyTable() {
        javafx.application.Platform.runLater(() -> {
           LOGGER.trace("refreshGeneOntologyTable: got a total of " + goPvals.size() + " Go Pval objects");
            goPvalTableView.getItems().clear(); /* clear previous rows, if any */
            goPvalTableView.getItems().addAll(goPvals);
            goPvalTableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        });
    }


    @FXML
    private void dgeOrDasDisplayBtn(ActionEvent actionEvent) {
        actionEvent.consume();
        GoComparison comparison = isopretService.getGoComparison();
        GoDisplayWidget widget = new GoDisplayWidget(comparison, this.comparisonMode);
        widget.show((Stage) this.goSummaryLabel.getScene().getWindow());
    }


    @FXML
    private void compareDgeDasBtncompareDgeDas(ActionEvent actionEvent) {
        actionEvent.consume();
        GoComparison comparison = isopretService.getGoComparison();
        GoCompWidget widget = new GoCompWidget(comparison);
        widget.show((Stage) this.goSummaryLabel.getScene().getWindow());
    }

    /**
     * This method is called to export an HTML file with SVGs for all genes
     * that are significant (either with respect to expression or splicing)
     * and are annotated to a certain GO term
     * @param goId The Gene Ontology term of interest
     */
    private void openExport(TermId goId) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Export " + goId);
        alert.setHeaderText("Export Information about " + goId);
        alert.setContentText(String.format("Export Information about differentially spliced genes annotated to %s",goId.getValue()));
        ButtonType buttonTypeOne = new ButtonType("Export");
        ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(buttonTypeOne, buttonTypeCancel);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get().equals(buttonTypeOne) && this.comparisonMode.equals(GeneOntologyComparisonMode.DAS)){
            GoHtmlExporter export = GoHtmlExporter.splicing(goId, isopretService);
            String html = export.export();
            Optional<File> f = getDefaultFname("splicing", goId.getValue());
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
        } else if (result.isPresent() && result.get().equals(buttonTypeOne) && this.comparisonMode.equals(GeneOntologyComparisonMode.DGE)){
            GoHtmlExporter export = GoHtmlExporter.expression(goId, isopretService);
            String html = export.export();
            Optional<File> f = getDefaultFname("expressed", goId.getValue());
            if (f.isEmpty()) {
                PopupFactory.displayError("Error", "Could not retrieve file.");
                return;
            }
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(f.get()))) {
                bw.write(html);
            } catch (IOException e) {
                PopupFactory.displayException("error", e.getMessage(), e);
            }            alert.close();
        } else {
            alert.close();
        }
    }

    Optional<File> getDefaultFname(String type, String goId) {
        Optional<File> hba = isopretService.getHbaDealsFileOpt();
        String hbaName = hba.map(File::getName).orElse("hbadeals");
        String fname = "isopret-" + type + "-" + goId + "-" + hbaName + ".html";
        fname = fname.replaceAll(":", "_");
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("HTML files (*.html)", "*.html");
        fileChooser.getExtensionFilters().add(extFilter);
        fileChooser.setInitialFileName(fname);
        Stage stage = (Stage) this.dgeOrDasGoBtn.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);
        return Optional.ofNullable(file);
    }
}
