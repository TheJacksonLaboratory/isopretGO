package org.jax.isopret.gui.controller;


import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.jax.isopret.core.InterproAnalysisResults;
import org.jax.isopret.core.IsopretInterpoAnalysisRunner;
import org.jax.isopret.core.analysis.InterproFisherExact;
import org.jax.isopret.core.analysis.InterproOverrepResult;
import org.jax.isopret.core.impl.rnaseqdata.RnaSeqAnalysisMethod;
import org.jax.isopret.model.GoMethod;
import org.jax.isopret.model.MtcMethod;
import org.jax.isopret.visualization.InterproOverrepVisualizer;
import org.jax.isopret.gui.configuration.ApplicationProperties;
import org.jax.isopret.gui.service.*;
import org.jax.isopret.gui.widgets.IsopretStatsWidget;
import org.jax.isopret.gui.widgets.PopupFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;

import static org.jax.isopret.gui.service.model.GeneOntologyComparisonMode.DAS;
import static org.jax.isopret.gui.service.model.GeneOntologyComparisonMode.DGE;
import static org.jax.isopret.gui.widgets.PopupFactory.displayIsopretThrown;

/**
 * A Java app to help design probes for Capture Hi-C
 * @author Peter Robinson
 * @version 1.0.3 (2022-08-07)
 */
@Component
public class MainController implements Initializable {
    private final static Logger LOGGER = LoggerFactory.getLogger(MainController.class.getName());
    public Tab dgeTab;
    public Tab dasTab;
    public Tab interproTab;

    @FXML
    private Label rnaSeqFileLabel;
    @FXML
    private ProgressBar analysisPB;
    @FXML
    private Label analysisLabel;

    @FXML
    private BorderPane rootNode;
    @FXML
    private Label downloadDataSourceLabel;
    @FXML
    private ProgressIndicator datasourcesDownloadProgressIndicator;
    private final ObservableList<String> goMethodList = FXCollections.observableArrayList("Term for Term",
            "Parent-Child Union", "Parent-Child Intersect");
    @FXML
    private ChoiceBox<String> goChoiceBox;
    private final ObservableList<String> mtcMethodList = FXCollections.observableArrayList(
            "Bonferroni", "Bonferroni-Holm","Sidak","Benjamini-Hochberg","Benjamini-Yekutieli", "None");

    /** The tab pane with setup, analysis, gene views. etc */
    @FXML
    TabPane tabPane;
    /** The 'second' tab of IsopretFX that shows a summary of the analysis and a list of Viewpoints.  */
    @FXML
    private Tab analysisTab;

    @FXML
    private ChoiceBox<String> mtcChoiceBox;

    @Autowired
    private IsopretService service;

    @Autowired
    private AnalysisController analysisController;

    @Autowired
    private Properties pgProperties;

    @Autowired
    private ApplicationProperties applicationProperties;

    @Autowired
    ResourceLoader resourceLoader;

    @Autowired
    HostServicesWrapper hostServicesWrapper;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.downloadDataSourceLabel.textProperty().bind(service.downloadDirProperty());
        this.datasourcesDownloadProgressIndicator.progressProperty().bind(service.downloadCompletenessProperty());
        this.rnaSeqFileLabel.textProperty().bind(service.rnaSeqResultsFileProperty());
        goChoiceBox.setItems(goMethodList);
        goChoiceBox.getSelectionModel().selectFirst();
        goChoiceBox.valueProperty().addListener((observable, oldValue, newValue) -> service.setGoMethod(newValue));
        mtcChoiceBox.setItems(mtcMethodList);
        mtcChoiceBox.getSelectionModel().selectFirst();
        mtcChoiceBox.valueProperty().addListener((observable, oldValue, newValue) -> service.setMtcMethod(newValue));
        analysisPB.setProgress(0.0);
    }




    @FXML
    private void downloadSources(ActionEvent e) {
        e.consume();
        if (service.sourcesDownloaded() > 0.9999999) { // don't worry about rounding errors
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
        LOGGER.info("Downloading files for isopret to {}", file.getAbsolutePath());

        IsopretFxDownloadTask task = new IsopretFxDownloadTask(file.getAbsolutePath());
        this.datasourcesDownloadProgressIndicator.progressProperty().unbind();
        this.datasourcesDownloadProgressIndicator.progressProperty().bind(task.progressProperty());
        this.downloadDataSourceLabel.textProperty().unbind();
        this.downloadDataSourceLabel.textProperty().bind(task.messageProperty());


        task.setOnSucceeded(c -> {
            LOGGER.info("Downloaded files for isopret to {}", file.getAbsolutePath());
            service.setDownloadDir(file);
            downloadDataSourceLabel.textProperty().unbind();
            downloadDataSourceLabel.textProperty().bind(service.downloadDirProperty());
        });
        task.setOnFailed(c -> {
            LOGGER.info("Could not downloaded files for isopret to {}", file.getAbsolutePath());
            service.setDownloadDir(null);
        });
        task.setOnCancelled(c -> LOGGER.info("download canceled"));
        Thread t = new Thread(task);
        Thread.UncaughtExceptionHandler h = (thread, throwable) ->
                Platform.runLater(() -> displayIsopretThrown(throwable));
        t.setUncaughtExceptionHandler(h);
        t.start();
    }

    /** Show version and last build time. */
    @FXML
    private void about(ActionEvent e) {
        String version = "1.0.3";
        if (applicationProperties.getApplicationVersion() != null) {
            version = applicationProperties.getApplicationVersion();
        }
        Instant lastTime = Instant.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
                .withLocale( Locale.UK )
                .withZone( ZoneId.systemDefault() );
        String lastChangedDate = formatter.format(lastTime);
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

    @FXML
    private void isopretAnalysis(ActionEvent actionEvent) {
        LOGGER.trace("Doing isopret analysis");
        // close any previous tabs, but keeping the setup and analysis tab
        ObservableList<Tab> panes = this.tabPane.getTabs();
        // If tabs are still open from a previous analysis, close them first
        // collect tabs first then remove them -- avoids a ConcurrentModificationException
        List<Tab> tabsToBeRemoved=new ArrayList<>();
        // close all tabs except setup and analysis.
        for (Tab tab : panes) {
            String id=tab.getId();
            if (id != null && (id.equals("setupTab") || id.equals("analysisTab") )) { continue; }
            tabsToBeRemoved.add(tab);
        }
        this.tabPane.getTabs().removeAll(tabsToBeRemoved);
        // clear the previous analysis result
        this.analysisController.clearPreviousResults();
        Optional<File> downloadOpt = service.getDownloadDir();
        if (downloadOpt.isEmpty()) {
            PopupFactory.displayError("ERROR", "Could not find download directory");
            return;
        }
        Optional<File> rnaSeqFileOpt = service.getRnaSeqResultsFileOpt();
        if (rnaSeqFileOpt.isEmpty()) {
            PopupFactory.displayError("ERROR", "RNA-Seq results file not found");
            return;
        }
        String goString = this.goChoiceBox.getValue();
        GoMethod goMethod = GoMethod.fromString(goString);
        String mtcString = this.mtcChoiceBox.getValue();
        MtcMethod mtcMethod = MtcMethod.fromString(mtcString);
        RnaSeqAnalysisMethod rnaSeqAnalysisMethod = service.getRnaSeqMethod();
        IsopretDataLoadTask task = new IsopretDataLoadTask(downloadOpt.get(),
                rnaSeqFileOpt.get(),
                goMethod,
                mtcMethod,
                rnaSeqAnalysisMethod);

        this.analysisLabel.textProperty().bind(task.messageProperty());
        this.analysisPB.progressProperty().unbind();
        this.analysisPB.progressProperty().bind(task.progressProperty());
        task.setOnSucceeded(event -> {
            LOGGER.trace("Finished Gene Ontology analysis of HBA-DEALS results");
            this.service.setData(task); // add the results of analysis to Service
            this.analysisController.refreshListView(); // show stats.
            this.analysisController.refreshVPTable(); // uses HbaDealsGeneRow objects to populate table etc.
            SingleSelectionModel<Tab> selectionModel = tabPane.getSelectionModel();
            selectionModel.select(this.analysisTab);
            try {
                Resource r = resourceLoader.getResource(
                        "classpath:fxml/geneOntologyPane.fxml");
                if (! r.exists()) {
                    LOGGER.error("Could not initialize Gene Ontology pane (fxml file not found)");
                    return;
                }
                FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(r.getURL()));
                loader.setControllerFactory(c -> new GeneOntologyController(DGE,  service.getDgeGoTerms(), service, hostServicesWrapper));
                ScrollPane p = loader.load();
                dgeTab = new Tab("DGE");
                dgeTab.setId("DGE");
                dgeTab.setClosable(false);
                dgeTab.setContent(p);
                this.tabPane.getTabs().add(dgeTab);
                GeneOntologyController gc1 = loader.getController();
                gc1.refreshGeneOntologyTable();
                interproTab = new Tab("Interpro");
                interproTab.setClosable(false);
            } catch (IOException e) {
                e.printStackTrace();
            }
            // Now the same for DAS
            try {
                Resource r = resourceLoader.getResource(
                        "classpath:fxml/geneOntologyPane.fxml");
                if (! r.exists()) {
                    LOGGER.error("Could not initialize Gene Ontology pane (fxml file not found)");
                    return;
                }
                FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(r.getURL()));
                loader.setControllerFactory(c -> new GeneOntologyController(DAS,service.getDasGoTerms(), service, hostServicesWrapper));
                ScrollPane p = loader.load();
                dasTab = new Tab("DAS");
                dasTab.setId("DAS");
                dasTab.setClosable(false);
                dasTab.setContent(p);
                this.tabPane.getTabs().add(dasTab);
                GeneOntologyController gc2 = loader.getController();
                gc2.refreshGeneOntologyTable();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // now for interpro
            try {
                Resource r = resourceLoader.getResource(
                        "classpath:fxml/interproPane.fxml");
                if (! r.exists()) {
                    LOGGER.error("Could not initialize Interpro pane (fxml file not found)");
                    return;
                }
                FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(r.getURL()));
                double splicingPepThreshold = service.getSplicingPepThreshold();
                InterproFisherExact ife = new InterproFisherExact(service.getAnnotatedGeneList(), splicingPepThreshold);
                List<InterproOverrepResult> results = ife.calculateInterproOverrepresentation();
                LOGGER.info("Got {} interpro overrepresentation results.", results.size());
                Collections.sort(results);
                loader.setControllerFactory(c -> new InterproController(results, service, hostServicesWrapper));
                ScrollPane p = loader.load();
                interproTab = new Tab("Interpro");
                interproTab.setId("Interpro");
                interproTab.setClosable(false);
                interproTab.setContent(p);
                this.tabPane.getTabs().add(interproTab);
                InterproController interproController = loader.getController();
                interproController.refreshTable();

            }catch (IOException e) {
                e.printStackTrace();
            }
        });
        task.setOnFailed(eh -> {
            Exception exc = (Exception)eh.getSource().getException();
            eh.getSource().getException().printStackTrace();
            //eh.getSource().getException().toString()
            PopupFactory.displayException("Error",
                    "Exception encountered while attempting to perform isopret analysis",
                    exc);
            this.analysisLabel.textProperty().unbind();
            this.analysisLabel.textProperty().setValue("Analysis failed: " + exc.getMessage());
        });
        Thread t = new Thread(task);
        Thread.UncaughtExceptionHandler h = (thread, throwable) ->
                Platform.runLater(() -> displayIsopretThrown(throwable));
        t.setUncaughtExceptionHandler(h);
        t.start();
    }

    public TabPane getMainTabPaneRef() {
        return this.tabPane;
    }

    /**
     * This method is called if the user chooses the help menu item and opens
     * the readthedoc documentation in the system menu.
     */
    public void openRTDhelp(ActionEvent e) {
       PopupFactory.openRTD(hostServicesWrapper);
       e.consume();
    }

    public void showStats(ActionEvent actionEvent) {
        Optional<File> opt = service.getRnaSeqResultsFileOpt();
        if (opt.isEmpty()) {
            PopupFactory.displayError("Error", "Cannot show stats before selecting HBA-DEALS file");
            return;
        }
        File f = opt.get();
        String basename = f.getName();
        IsopretStatsWidget widget = new IsopretStatsWidget(service.getIsopretStats(), basename);
        widget.show();
        actionEvent.consume();
    }

    public void exportGoReport(ActionEvent actionEvent) {
        String report = service.getGoReport();
        FileChooser chooser = new FileChooser();
        chooser.setInitialDirectory(new File(System.getProperty("user.home")));
        chooser.setTitle("Save GO Overrepresentation results");
        Optional<String> opt = service.getGoReportDefaultFilename();
        if (opt.isEmpty()) {
            PopupFactory.displayError("Error", "could not retrieve file name");
            return; // should never happen
        }
        chooser.setInitialFileName(opt.get());
        File file = chooser.showSaveDialog(rootNode.getScene().getWindow());
        if (file==null || file.getAbsolutePath().equals("")) {
            String msg = "Could not get  GO Overrepresentation file for saving";
            LOGGER.error(msg);
            PopupFactory.displayError("Error",msg);
            return;
        }
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            bw.write(report);
        } catch (IOException e) {
            PopupFactory.displayException("Error", "Could not write GO report", e);
        }
    }

    public void exportInterproReport(ActionEvent event) {
        event.consume();
        double splicingPepThreshold = service.getSplicingPepThreshold();
        IsopretInterpoAnalysisRunner runner = IsopretInterpoAnalysisRunner.hbadeals(service.getAnnotatedGeneList(), splicingPepThreshold);
        InterproAnalysisResults results = runner.run();

        FileChooser chooser = new FileChooser();
        chooser.setInitialDirectory(new File(System.getProperty("user.home")));
        chooser.setTitle("Save Isopret domain Overrepresentation results");
        Optional<String> opt = service.getGoReportDefaultFilename();
        if (opt.isEmpty()) {
            PopupFactory.displayError("Error", "could not retrieve file name");
            return; // should never happen
        }
        chooser.setInitialFileName(opt.get());
        File file = chooser.showSaveDialog(rootNode.getScene().getWindow());
        if (file==null || file.getAbsolutePath().equals("")) {
            String msg = "Could not get Isopret domain overrepresentation file for saving";
            LOGGER.error(msg);
            PopupFactory.displayError("Error",msg);
            return;
        }
        InterproOverrepVisualizer visualizer = new InterproOverrepVisualizer(results);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            bw.write(visualizer.getTsv());
        } catch (IOException e) {
            PopupFactory.displayException("Error", "Could not write GO report", e);
        }

    }

    private void chooseRnaSeqFile(RnaSeqAnalysisMethod method, String methodName) {
        FileChooser chooser = new FileChooser();
        chooser.setInitialDirectory(new File(System.getProperty("user.home")));
        chooser.setTitle("Choose "+ methodName + "File");
        File file = chooser.showOpenDialog(rootNode.getScene().getWindow());
        if (file==null || file.getAbsolutePath().equals("")) {
            LOGGER.error("Could not get {} file", methodName);
            PopupFactory.displayError("Error","Could not get " + methodName + " file.");
            return;
        }
        service.setRnaSeqFile(file, method);
    }

    /**
     * Show the user a file chooser to select an output (results) file from HBA-DEALS.
     */
    @FXML
    private void chooseHbaDealsOutputFile(ActionEvent e) {
        e.consume();
        chooseRnaSeqFile(RnaSeqAnalysisMethod.HBADEALS, "HBA-DEALS");
    }
    /**
     * Show the user a file chooser to select an output (results) file from edgeR.
     */
    public void chooseEdgeRFile(ActionEvent e) {
        e.consume();
        chooseRnaSeqFile(RnaSeqAnalysisMethod.EDGER, "edgeR");
    }
}
