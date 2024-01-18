package org.jax.isopret.gui.widgets;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;
import org.jax.isopret.visualization.GoAnnotationMatrix;
import org.jax.isopret.visualization.GoAnnotationRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class GoTableGeneratorPopup {
    private static final Logger LOGGER = LoggerFactory.getLogger(GoTableGeneratorPopup.class);
    private final GoAnnotationMatrix goMatrix;

    private final String geneSymbol;
    private final String geneAccession;

    private final String basename;
    public GoTableGeneratorPopup(GoAnnotationMatrix matrix, String baseName) {
        goMatrix = matrix;
        geneSymbol = goMatrix.getGeneSymbol();
        geneAccession = goMatrix.getAccession();
        basename = baseName;
        Alert dialog = new Alert(Alert.AlertType.INFORMATION);
        dialog.setTitle("Export GO annotations");
        dialog.setHeaderText("Choose export format");
        dialog.setResizable(false);
        dialog.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        dialog.getDialogPane().setMinWidth(Region.USE_PREF_SIZE);

        ButtonType latexButtonType = new ButtonType("LaTeX");
        ButtonType tsvButtonType = new ButtonType("TSV");
        ButtonType cancelButtonType = new ButtonType("Cancel");

        dialog.getButtonTypes().clear();
        dialog.getButtonTypes().addAll(latexButtonType, tsvButtonType, cancelButtonType);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.get() == latexButtonType){
            outputLatex(goMatrix);
        } else if (result.get() == tsvButtonType) {
            outputTsv(goMatrix);
        } else {
            dialog.close();
        }

    }


    private List<String> getHeaderFields(GoAnnotationMatrix matrix) {
        List<String> transcriptList = goMatrix.getExpressedCodingTranscripts();
        transcriptList.add(0, "GO term significant");
        transcriptList.add(0, "GO id");
        transcriptList.add(0, "GO label");
        return transcriptList;
    }

    private void outputLatex(GoAnnotationMatrix goMatrix) {
        List<String> lines = new ArrayList<>(); // rows of output LaTeX code
       lines.add("\\documentclass{article}");
       lines.add("\\usepackage{array,graphicx}");
       lines.add("\\usepackage[margin=0.5in]{geometry}");
        lines.add("\\usepackage{booktabs}");
        lines.add("\\usepackage{pifont}");
        lines.add("\\usepackage{times}");
        lines.add("\\usepackage{booktabs}");
        lines.add("\\newcommand*\\rot{\\rotatebox{90}}");
        lines.add("\\newcommand*\\OK{\\ding{51}}");
        lines.add("\\usepackage{color, colortbl}");
        lines.add("\\definecolor{green}{rgb}{0.20,0.9,0.2}");

        List<GoAnnotationRow> rowList = goMatrix.getExpressedCodingAnnotationRows();
        List<String> transcriptList = goMatrix.getExpressedCodingTranscripts();
        int n_columns = transcriptList.size();

        lines.add("\\begin{document}");
        lines.add("\\pagestyle{empty}\n");
        lines.add("\\begin{table} \\centering");
        String tabLine = String.format("\\begin{tabular}{@{} cl*{%s}c @{}}", n_columns);
        lines.add(tabLine);
        lines.add("\\toprule");
        List<String> headerFields = new ArrayList<>();
        headerFields.add("");
        headerFields.add("GO Term");
        for (String transcript : transcriptList) {
            String rotatedTranscript = String.format("\\rot{%s}", transcript);
            headerFields.add(rotatedTranscript);
        }
        lines.add(String.join(" & ", headerFields) + "\\\\");
        lines.add("\\midrule");
        for (var row: rowList) {
            List<String> items = new ArrayList<>();
            items.add("");
            String goItem = String.format("%s (%s)", row.getGoLabel(), row.getGoId().getValue());
            items.add(goItem);
            List<Boolean> annotated = row.getTranscriptAnnotated();
            for (boolean ann : annotated) {
                if (ann) {
                    items.add("\\OK");
                } else {
                    items.add("");
                }
            }
            if ( row.isGoTermSignificant()) {
                lines.add("\\rowcolor{green}");
            }
            lines.add(String.join(" & ", items) + "\\\\");
        }
        lines.add("\\bottomrule");
        lines.add("\\end{tabular}");
        String caption = String.format("\\caption{GO Annotations (isopret) for isoforms of %s (%s).}",
                geneSymbol, geneAccession);
        lines.add(caption);
        lines.add("\\end{table}");
        lines.add("\\end{document}");
        String fileName = basename + ".tex";
        exportGoReport(String.join("\n", lines), "Save GO Annotations as LaTeX file", fileName);
    }

    private void outputTsv(GoAnnotationMatrix goMatrix) {
        List<String> lines = new ArrayList<>(); // rows of output table
        List<GoAnnotationRow> rowList = goMatrix.getExpressedCodingAnnotationRows();
        List<String> transcriptList = goMatrix.getExpressedCodingTranscripts();
        transcriptList.add(0, "GO id");
        transcriptList.add(0, "GO label");
        transcriptList.add(0, "GO term significant");
        lines.add(String.join("\t", transcriptList)); // this will become the header line
        for (var row: rowList) {
            List<String> items = new ArrayList<>();
            items.add(row.getGoId().getValue());
            items.add(row.getGoLabel());
            String goSig = row.isGoTermSignificant() ? "yes" : "no";
            items.add(goSig);
            List<Boolean> annotated = row.getTranscriptAnnotated();
            for (boolean ann : annotated) {
                if (ann) {
                    items.add("yes");
                } else {
                    items.add("no");
                }
            }
            lines.add(String.join("\t", items));
        }
        String fileName = basename + ".tsv";
        exportGoReport(String.join("\n", lines), "Save GO Annotations as TSV file", fileName);
    }


    public void exportGoReport(String contents, String title, String initialFileName) {
        FileChooser chooser = new FileChooser();
        chooser.setInitialDirectory(new File(System.getProperty("user.home")));
        chooser.setTitle(title);
        chooser.setInitialFileName(initialFileName);
        File file = chooser.showSaveDialog(null);
        if (file==null || file.getAbsolutePath().isEmpty()) {
            String msg = "Could not get  file name for saving";
            LOGGER.error(msg);
            PopupFactory.displayError("Error",msg);
            return;
        }
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            bw.write(contents);
        } catch (IOException e) {
            PopupFactory.displayException("Error", "Could not write GO annotations", e);
        }
    }


    private void saveMapToFile(Map<String, String> allEntries, File file) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (var entry : allEntries.entrySet()) {
                writer.write(entry.getKey() + "\t" + entry.getValue() + "\n");
            }
        } catch (IOException e) {
            PopupFactory.displayException("Error", "Could not write settings to file", e);
        }
    }
}
