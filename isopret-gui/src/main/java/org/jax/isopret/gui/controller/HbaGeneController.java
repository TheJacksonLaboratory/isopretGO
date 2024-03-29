package org.jax.isopret.gui.controller;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.io.IOUtils;
import org.jax.isopret.gui.widgets.GoTableGeneratorPopup;
import org.jax.isopret.visualization.*;
import org.jax.isopret.gui.service.HostServicesWrapper;
import org.jax.isopret.gui.service.IsopretService;
import org.jax.isopret.gui.widgets.PopupFactory;
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
import java.nio.charset.Charset;
import java.util.*;

@Component
@Scope("prototype")
public class HbaGeneController implements Initializable {
    private static final Logger LOGGER = LoggerFactory.getLogger(HbaGeneController.class.getName());
    public TextFlow goAnnotationsTextFlow;

    @FXML
    private TableView<IsoformVisualizable> isoformTableView;
    @FXML
    private TableColumn<IsoformVisualizable, String> accessionColumn;
    @FXML
    private TableColumn<IsoformVisualizable, Button> urlColumn;
    @FXML
    private TableColumn<IsoformVisualizable, String> isoformLogFcColumn;
    @FXML
    private TableColumn<IsoformVisualizable, String> isoformPColumn;

    @FXML
    private  TableView<InterproVisualizable> interproTableView;
    @FXML
    private TableColumn<InterproVisualizable, String> interproAccessionColumn;
    @FXML
    private TableColumn<InterproVisualizable, String> interproEntryType;
    @FXML
    private TableColumn<InterproVisualizable, String> interproDescription;
    @FXML
    private VBox hbaGeneVbox;
    @FXML
    private WebView hbaGeneWebView;
    @FXML
    private WebView hbaProteinWebView;
    @FXML
    private WebView hbaGoWebView;

    private final HostServicesWrapper hostServicesWrapper;

    private final IsopretService service;

    private final Visualizable visualizable;

    public HbaGeneController(Visualizable visualisable, IsopretService isoservice, HostServicesWrapper wrapper) {
        this.visualizable = visualisable;
        this.service = isoservice;
        this.hostServicesWrapper = wrapper;
    }



    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Text geneSymbolText = new Text(String.format("Gene Ontology annotations for %s (%s)\n",
                visualizable.getGeneSymbol(),
                visualizable.getGeneAccession()));
        geneSymbolText.setFont(Font.font("Verdana", FontWeight.BOLD,16));
        Set<TermId> annotatingGoTerms = visualizable.getAnnotationGoIds();
        int total = annotatingGoTerms.size();
        int signf = service.totalSignificantGoTermsAnnotatingGene(annotatingGoTerms);
        Text explanation;
        if (total == 0) {
            explanation = new Text("No annotating GO terms were found");
        } else if (signf == 1) {
            explanation = new Text(String.format("%d GO terms annotate %s, of which 1 was significant for DGE or DAS.\n",
                    total, visualizable.getGeneSymbol()));
        } else {
            explanation = new Text(String.format("%d GO terms annotate %s, of which %d were significant for DGE or DAS.\n",
                    total, visualizable.getGeneSymbol(), signf));
        }
        explanation.setFont(Font.font("Verdana", FontWeight.NORMAL,12));
        Text transcriptText = new Text(String.format("%s has %d transcripts, of which %d were found to be expressed in this dataset and are shown here.\n",
                visualizable.getGeneSymbol(), visualizable.getTotalTranscriptCount(), visualizable.getExpressedTranscriptCount()));
        this.goAnnotationsTextFlow.getChildren().addAll(geneSymbolText, explanation,transcriptText);
        accessionColumn.setSortable(false);
        accessionColumn.setEditable(false);
        accessionColumn.setCellValueFactory(v ->  new ReadOnlyStringWrapper(v.getValue().transcriptAccession()));
        urlColumn.setEditable(false);
        urlColumn.setSortable(false);

        urlColumn.setCellValueFactory(cdf -> {
            IsoformVisualizable vis = cdf.getValue();
            String acc = vis.transcriptAccession();
            String ensemblUrl = "https://www.ensembl.org/Homo_sapiens/Transcript/Summary?db=core;t=" +acc;            Button btn = new Button("Ensembl");
            btn.setOnAction(e -> { this.hostServicesWrapper.showDocument(ensemblUrl);
                LOGGER.trace(String.format("Calling URL: %s", ensemblUrl));
            });
            // wrap it so it can be displayed in the TableView
            return new ReadOnlyObjectWrapper<>(btn);
        });

        isoformLogFcColumn.setSortable(false);
        isoformLogFcColumn.setEditable(false);
        isoformLogFcColumn.setCellValueFactory(v -> new ReadOnlyStringWrapper(v.getValue().log2Foldchange()));
        isoformPColumn.setSortable(false);
        isoformPColumn.setEditable(false);
        isoformPColumn.setCellValueFactory(v -> new ReadOnlyStringWrapper(v.getValue().isoformP()));
        isoformTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN); // do not show "extra column"
        LOGGER.trace("Adding isoform vis n={} items", visualizable.getIsoformVisualizable().size());
        WebEngine webEngine = hbaGeneWebView.getEngine();
        webEngine.loadContent(this.visualizable.getIsoformHtml());
        // interpro table
        interproAccessionColumn.setEditable(false);
        interproAccessionColumn.setSortable(false);
        interproAccessionColumn.setCellValueFactory(v -> new ReadOnlyStringWrapper(v.getValue().getInterproAccession()));
        interproEntryType.setEditable(false);
        interproEntryType.setSortable(false);
        interproEntryType.setCellValueFactory(v -> new ReadOnlyStringWrapper(v.getValue().getEntryType()));
        interproDescription.setEditable(false);
        interproDescription.setSortable(false);
        interproDescription.setCellValueFactory(v -> new ReadOnlyStringWrapper(v.getValue().getDescription()));
        interproTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN); // do not show "extra column"
        WebEngine interprebEngine = hbaProteinWebView.getEngine();
        interprebEngine.loadContent(this.visualizable.getProteinHtml());

    }

    /**
     * Add content to the tables.
     */
    public void refreshTables() {
        javafx.application.Platform.runLater(() -> {
            isoformTableView.getItems().clear();
            List<IsoformVisualizable> visualizableList = new ArrayList<>();
            visualizableList.add(new EnsemblGeneIsoformVisualizable(visualizable));
            visualizableList.addAll(visualizable.getIsoformVisualizable());
            isoformTableView.getItems().addAll(visualizableList);
            isoformTableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
            isoformTableView.setFixedCellSize(30);
            isoformTableView.prefHeightProperty().bind(Bindings.size(isoformTableView.getItems()).multiply(isoformTableView.getFixedCellSize()).add(40));
            hbaGeneWebView.setMaxHeight(visualizable.getIsoformSvgHeight());
            hbaProteinWebView.setMaxHeight(visualizable.getProteinSvgHeight()+30);
            interproTableView.getItems().clear();
            interproTableView.getItems().addAll(visualizable.getInterproVisualizable());
            interproTableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
            interproTableView.setFixedCellSize(25);
            interproTableView.prefHeightProperty().bind(Bindings.size(interproTableView.getItems()).multiply(isoformTableView.getFixedCellSize()).add(40));
            WebEngine goEngine = hbaGoWebView.getEngine();
            String goTable = this.visualizable.getGoHtml();
            String html = HtmlUtil.cssWrap(goTable);
            goEngine.loadContent(html);
        });
    }

    @FXML private void htmlSummaryExport(ActionEvent e) {

        Optional<File> opt =  service.getRnaSeqResultsFileOpt();
        if (opt.isEmpty()) {
            PopupFactory.displayError("Error", "Could not get HBA-DEAL file name");
            return;
        }
        File isopretFile = opt.get();
        String basename = isopretFile.getName();
        String genesymbol = visualizable.getGeneSymbol();
        String fname = basename +"-" + genesymbol + "-isopret.html";
        final HtmlVisualizer visualizer = new HtmlVisualizer(basename);
        String html = visualizer.getHtml(this.visualizable);
        html = HtmlUtil.wrapWithGoTableWidth(html, visualizable.getGoTableWidth());
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("HTML files (*.html)", "*.html");
        fileChooser.getExtensionFilters().add(extFilter);
        fileChooser.setInitialFileName(fname);
        Stage stage = (Stage) this.interproTableView.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);
        if (file == null) {
            PopupFactory.displayError("Error", "Could not retrieve file.");
            return;
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(html);
        } catch (IOException ex) {
            PopupFactory.displayException("Error", "Could not write html", ex);
        }
    }

    @FXML private void geneSVGexport(ActionEvent e) {
        String svg = this.visualizable.getIsoformSvg();
        Optional<File> opt = service.getRnaSeqResultsFileOpt();
        if (opt.isEmpty()) {
            PopupFactory.displayError("Error", "Cannot export isoform SVG because HBA-DEALS file name not found");
            return;
        }
        File isopretFile = opt.get();
        String basename = isopretFile.getName();
        String genesymbol = visualizable.getGeneSymbol();
        String fname = basename +"-" + genesymbol + "-isoforms.svg";
        saveSvgToFile(svg, fname);
    }

    @FXML private void genePDFexport(ActionEvent e) {
        Optional<File> opt = service.getRnaSeqResultsFileOpt();
        if (opt.isEmpty()) {
            PopupFactory.displayError("Error", "Cannot export SVG because HBA-DEALS file name not found");
            return;
        }
        File isopretFile = opt.get();
        String basename = isopretFile.getName();
        String genesymbol = visualizable.getGeneSymbol();
        String fname = basename +"-" + genesymbol + "-isoform.pdf";
        Optional<File> optFile = getPdfFileForSaving(fname);
        if (optFile.isEmpty()) {
            PopupFactory.displayError("Error", "Could not retrieve file path for saving PDF.");
            return;
        }
        saveToPdf(visualizable.getIsoformSvg(), optFile.get());
    }

    @FXML private void proteinSVGexport(ActionEvent e) {
        String svg = this.visualizable.getProteinSvg();
        Optional<File> opt = service.getRnaSeqResultsFileOpt();
        if (opt.isEmpty()) {
            PopupFactory.displayError("Error", "Cannot export SVG because HBA-DEALS file name not found");
            return;
        }
        File isopretFile = opt.get();
        String basename = isopretFile.getName();
        String genesymbol = visualizable.getGeneSymbol();
        String fname = basename +"-" + genesymbol + "-domains.svg";
        saveSvgToFile(svg, fname);

    }

    @FXML private void proteinPDFexport(ActionEvent e) {
        Optional<File> opt = service.getRnaSeqResultsFileOpt();
        if (opt.isEmpty()) {
            PopupFactory.displayError("Error", "Cannot export SVG because HBA-DEALS file name not found");
            return;
        }
        File isopretFile = opt.get();
        String basename = isopretFile.getName();
        String genesymbol = visualizable.getGeneSymbol();
        String fname = basename +"-" + genesymbol + "-domains.pdf";
        Optional<File> optFile = getPdfFileForSaving(fname);
        if (optFile.isEmpty()) {
            PopupFactory.displayError("Error", "Could not retrieve file path for saving PDF.");
            return;
        }
        saveToPdf(visualizable.getProteinSvg(), optFile.get());
    }

    private void saveSvgToFile(String contents, String fname) {
        LOGGER.info("Saving data to {}", fname);
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("SVG files (*.svg)", "*.svg");
        fileChooser.getExtensionFilters().add(extFilter);
        fileChooser.setInitialFileName(fname);
        Stage stage = (Stage) this.isoformTableView.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);
        if (file == null) {
            PopupFactory.displayError("Error", "Could not retrieve file.");
            return;
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(contents);
        } catch (IOException e) {
            PopupFactory.displayException("Error", "Could not write data", e);
        }
    }

    private Optional<File> getPdfFileForSaving(String fname) {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("PDF files (*.pdf)", "*.pdf");
        fileChooser.getExtensionFilters().add(extFilter);
        fileChooser.setInitialFileName(fname);
        Stage stage = (Stage) this.isoformTableView.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);
        return Optional.ofNullable(file);
    }

    /**
     * @return "random" string to make a temp file
     */
    public String getRandomString() {
        int numChars = 30;
        final Random RAND = new Random();
        int chars = RAND.nextInt(numChars);
        while (chars == 0)
            chars = RAND.nextInt(numChars);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < chars; i++) {
            int index = 97 + RAND.nextInt(26);
            char c = (char) index;
            sb.append(c);
        }
        sb.append("-temp");
        return sb.toString();
    }

    /**
     * Creates a PDF file using {@code rsvg-convert -f pdf -o mygraph.pdf mygraph.svg}
     * @param contents SVG code
     * @param pdfFile path to write the corresponding PDF file
     */
    private void saveToPdf(String contents, File pdfFile) {
        // get temp file and save SVG
        File temp;
        try {
            temp = File.createTempFile(getRandomString(), ".svg");
            temp.deleteOnExit(); //Delete when JVM exits
            BufferedWriter writer = new BufferedWriter(new FileWriter(temp));
            writer.write(contents);
            writer.close();
        } catch (IOException e) {
            PopupFactory.displayException("Error", "Could not generate temp file to create PDF", e);
            return;
        }
        Runtime rt = Runtime.getRuntime();
        String[] commands = {"rsvg-convert", "-f" , "pdf", "-o",
                pdfFile.getAbsolutePath(), temp.getAbsolutePath() };
        Process proc = null;
        try {
            proc = rt.exec(commands);
            String stderr = IOUtils.toString(proc.getErrorStream(), Charset.defaultCharset());
            String stdout = IOUtils.toString(proc.getInputStream(), Charset.defaultCharset());
            if (stderr != null && ! stderr.isEmpty()) {
                PopupFactory.displayError("Could not write PDF file", stderr);
            }
            if (stdout != null && ! stdout.isEmpty()) {
                System.out.println(stdout);
            }
        } catch (IOException e) {
            if (proc == null) {
                PopupFactory.displayException("Error", "Null pointer process", e);
                return;
            }
            proc.destroy();
            String errMsg =  String.format("Could not create PDF file - have you installed rsvg-convert?  (Exit code %d)", proc.exitValue());
            PopupFactory.displayException("Error", errMsg, e);
            return;
        }
        PopupFactory.showInfoMessage( "Successfully created " + pdfFile.getAbsolutePath(), "Info");
    }

    /**
     * Export a TSV file with isopret annotations for this gene
     */
    @FXML
    public void goAnnotExport(ActionEvent e) {
        e.consume();
        Optional<String> opt = service.getGoReportDefaultFilename();
        String baseName;
        if (opt.isPresent()) {
            baseName = opt.get().replace(".tsv", "");
            baseName = String.format("%s-%s", baseName, visualizable.getGeneSymbol());
        } else {
            baseName = visualizable.getGeneSymbol();
        }
        GoAnnotationMatrix goMatrix = visualizable.getGoAnnotationMatrix();
        GoTableGeneratorPopup popup = new GoTableGeneratorPopup(goMatrix, baseName);

    }
}
