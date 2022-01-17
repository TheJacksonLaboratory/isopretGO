package org.jax.isopret.gui.widgets;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import java.util.LinkedHashMap;
import java.util.Map;

public class IsopretStatsWidget {

    private final IsopretStats isopretStats;

    public IsopretStatsWidget(IsopretStats stats) {
        this.isopretStats = stats;
    }

    public void show() {
        Text text = new Text("Statistics for isopret analysis");
        text.setFont(Font.font("Verdana", FontWeight.BOLD,16));
        TextFlow tflow = new TextFlow();
        tflow.getChildren().add(text);
        VBox vbox = new VBox();
        vbox.getChildren().add(tflow);
        Map<String, String> statsMap = isopretStats.getAllEntries();
        ObservableList<String> keys = FXCollections.observableArrayList(statsMap.keySet());
        ObservableList<String> values = FXCollections.observableArrayList(statsMap.values());
        ListView<String> lviewKey = new ListView<>(keys);
        ListView<String> lviewValue = new ListView<>(values);
        HBox listViewBox = new HBox();
        listViewBox.getChildren().addAll(lviewKey, lviewValue);
        vbox.getChildren().add(listViewBox);
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
            File file = fileChooser.showSaveDialog(newWindow);
            if (file != null) {
                saveMapToFile(isopretStats.getAllEntries(), file);
            }
        });

        HBox buttonBox = new HBox();
        buttonBox.getChildren().addAll(saveButton, closeButton);
        vbox.getChildren().add(buttonBox);
        ScrollPane spane = new ScrollPane(vbox);
        Scene scene = new Scene(spane, 600, 800);
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
