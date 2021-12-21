package org.jax.isopret.gui.controller;


import javafx.application.HostServices;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import org.jax.isopret.gui.service.IsopretService;
import org.jax.isopret.gui.service.model.GoTermAndPvalVisualized;
import org.jax.isopret.gui.service.model.HbaDealsGeneRow;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.stats.GoTerm2PValAndCounts;
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
    private final String methodsLabel;
    private final String summaryLabel;
    public Label goTopLevelLabel;
    public Label goMethodsLabel;
    public Label goSummaryLabel;

    private final IsopretService isopretService;


    public GeneOntologyController(String topLevelLabel,  List<GoTerm2PValAndCounts> pvals, IsopretService service) {
        this.label = topLevelLabel;
        this.methodsLabel = service.getGoMethods();
        this.summaryLabel = service.getGoSummary();
        this.isopretService = service;
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

        HostServices hostServices = isopretService.getHostServices();
        if (hostServices == null) {
            LOGGER.error("COuld not retrieve HostServices");
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


}
