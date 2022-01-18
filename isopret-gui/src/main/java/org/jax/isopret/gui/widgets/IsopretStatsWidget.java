package org.jax.isopret.gui.widgets;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.jax.isopret.core.analysis.IsopretStats;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

/**
 * Class to display a window that shows the descriptive stats about the current
 * HBA-DEALS run.
 * @author Peter N Robinson
 */
public class IsopretStatsWidget {

    private final IsopretStats isopretStats;
    /** The initial file name that will be suggested to the user, equivalent
     * to the name of the HBA-DEALS file plus -isopret-stats.txt
     */
    private final String defaultFileName;

    public IsopretStatsWidget(IsopretStats stats, String basename) {
        this.isopretStats = stats;
        this.defaultFileName = basename + "-isopret-stats.txt";
    }

    public void show() {
        if (isopretStats == null || isopretStats.getAllEntries().isEmpty()) {
            PopupFactory.displayError("Error", "Cannot show stats before analysis is finished");
            return;
        }
        Text text = new Text("Statistics for isopret analysis");
        text.setFont(Font.font("Verdana", FontWeight.BOLD,16));
        TextFlow tflow = new TextFlow();
        tflow.getChildren().add(text);
        VBox vbox = new VBox();
        //trbl
        vbox.setPadding(new Insets(20, 20, 10, 20));
        vbox.setSpacing(10);
        vbox.getChildren().add(tflow);
        Map<String, String> statsMap = isopretStats.getAllEntries();
        ObservableList<String> keys = FXCollections.observableArrayList(statsMap.keySet());
        ObservableList<String> values = FXCollections.observableArrayList(statsMap.values());
        ListView<String> lviewKey = new ListView<>(keys);
        ListView<String> lviewValue = new ListView<>(values);
        lviewKey.setMinWidth(300);
        lviewValue.setMinWidth(400);
        int minHeight = 700;
        lviewKey.setMinHeight(minHeight);
        lviewValue.setMinHeight(minHeight);
        HBox listViewBox = new HBox();

        listViewBox.getChildren().addAll(lviewKey, lviewValue);
        ScrollPane spane = new ScrollPane(listViewBox);
        vbox.getChildren().add(spane);
        Region region = new Region();
        region.setMinHeight(20);
        vbox.getChildren().add(region);
        Button saveButton = new Button("Save to file");
        Button closeButton = new Button("Close");
        Stage newWindow = new Stage();
        closeButton.setOnAction(e -> newWindow.close());
        saveButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt");
            fileChooser.getExtensionFilters().add(extFilter);
            fileChooser.setInitialFileName(defaultFileName);
            File file = fileChooser.showSaveDialog(newWindow);
            if (file != null) {
                saveMapToFile(isopretStats.getAllEntries(), file);
            }
        });

        HBox buttonBox = new HBox();
        buttonBox.setPadding(new Insets(2, 2, 1, 2));
        buttonBox.setSpacing(10);
        buttonBox.getChildren().addAll(saveButton, closeButton);
        vbox.getChildren().add(buttonBox);
        //ScrollPane spane = new ScrollPane(vbox);
        Scene scene = new Scene(vbox, 800, 800);
        newWindow.setTitle("Isopret Analysis Stats");
        newWindow.setScene(scene);
        newWindow.show();
    }

    private void saveMapToFile(Map<String, String> allEntries, File file) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (var entry : allEntries.entrySet()) {
                writer.write(entry.getKey() + "\t" + entry.getValue() + "\n");
            }
        } catch (IOException e) {
            PopupFactory.displayException("Error", "Could not write stats to file", e);
        }
    }


}
