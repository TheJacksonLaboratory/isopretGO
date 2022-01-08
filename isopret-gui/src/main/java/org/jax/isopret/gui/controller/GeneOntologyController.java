package org.jax.isopret.gui.controller;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.jax.isopret.gui.service.HostServicesWrapper;
import org.jax.isopret.gui.service.IsopretService;
import org.jax.isopret.gui.service.model.GeneOntologyComparisonMode;
import org.jax.isopret.gui.service.model.GoComparison;
import org.jax.isopret.gui.service.model.GoTermAndPvalVisualized;
import org.jax.isopret.gui.widgets.GoCompWidget;
import org.jax.isopret.gui.widgets.GoDisplayWidget;
import org.monarchinitiative.phenol.analysis.stats.GoTerm2PValAndCounts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Controller that is use to display the DGE or DAS data
 */
@Component
@Scope("prototype")
public class GeneOntologyController implements Initializable {
    private static final Logger LOGGER = LoggerFactory.getLogger(GeneOntologyController.class.getName());


    private final List<GoTermAndPvalVisualized> goPvals;
    /* Main pane of the GO Overpresentation tabs */
    public ScrollPane geneOntologyPane;
    public HBox listviewHbox;
    public TableView<GoTermAndPvalVisualized> goPvalTableView;
    public TableColumn<GoTermAndPvalVisualized, String> termColumn;
    public TableColumn<GoTermAndPvalVisualized, String> termIdColumn;
    public TableColumn<GoTermAndPvalVisualized, String> studyCountsColumn;
    public TableColumn<GoTermAndPvalVisualized, String> populationCountsColumn;
    public TableColumn<GoTermAndPvalVisualized, String> pvalColumn;
    public TableColumn<GoTermAndPvalVisualized, String> adjpvalColumn;
    public TableColumn<GoTermAndPvalVisualized, Button> amigoColumn;
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
        termIdColumn.setCellValueFactory(cdf -> new ReadOnlyStringWrapper(cdf.getValue().getGoTermId()));

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
            amigoColumn.setSortable(false);
            amigoColumn.setEditable(false);
            amigoColumn.setCellValueFactory(cdf -> {
                GoTermAndPvalVisualized geneRow = cdf.getValue();
                String termId = geneRow.getGoTermId();
                String amigoUrl = "http://amigo.geneontology.org/amigo/term/" + termId;
                Button btn = new Button("AmiGO");
                btn.setOnAction(e -> { hostServices.showDocument(amigoUrl);
                    LOGGER.trace(String.format("Calling URL: %s", amigoUrl));
                });
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
}
