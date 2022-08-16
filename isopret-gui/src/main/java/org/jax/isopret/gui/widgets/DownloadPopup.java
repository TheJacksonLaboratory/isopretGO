package org.jax.isopret.gui.widgets;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.jax.isopret.gui.service.HostServicesWrapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

public class DownloadPopup {

    @Autowired
    HostServicesWrapper hostServicesWrapper;

    private ObservableList<DownloadTableRow> rows;

    public DownloadPopup(Map<String, String> successulDownloads, Map<String, String> failedDownloads) {
        List<DownloadTableRow> myrows = new ArrayList<>();
        for (var e : successulDownloads.entrySet()) {
            DownloadTableRow dtr = new DownloadTableRow(e.getValue(), e.getKey(), true);
            myrows.add(dtr);
        }
        for (var e : failedDownloads.entrySet()) {
            DownloadTableRow dtr = new DownloadTableRow(e.getValue(), e.getKey(), false);
            myrows.add(dtr);
        }
        rows = FXCollections.observableArrayList();
        rows.addAll(myrows);
    }

    static record DownloadTableRow(String url, String basename, boolean successful) {

    }

    public void showDialog() {
        Stage window;
        String windowTitle = "Downloads";
        window = new Stage();
        window.setOnCloseRequest(event -> window.close());
        window.setTitle(windowTitle);

        TableView<DownloadTableRow> tableView = new TableView<>();
        TableColumn<DownloadTableRow, String> nameColumn = new TableColumn<>();
        TableColumn<DownloadTableRow, String> successColumn = new TableColumn<>();
        TableColumn<DownloadTableRow, String> urlColumn = new TableColumn<>();
        TableColumn<DownloadTableRow, Button> manualDownloadColumn = new TableColumn<>();
        tableView.getColumns().add(nameColumn);

        nameColumn.setSortable(true);
        nameColumn.setEditable(false);
        nameColumn.setCellValueFactory(cdf -> new ReadOnlyStringWrapper(cdf.getValue().basename()));

        successColumn.setSortable(true);
        successColumn.setEditable(false);
        successColumn.setCellValueFactory(cdf -> {
                    DownloadTableRow geneRow = cdf.getValue();
                    String suc = geneRow.successful() ? "OK" : "Failed";
                    return new ReadOnlyStringWrapper(suc);
                    });

        urlColumn.setSortable(true);
        urlColumn.setEditable(false);
        urlColumn.setCellValueFactory(cdf -> new ReadOnlyStringWrapper(cdf.getValue().basename()));

        manualDownloadColumn.setSortable(false);
        manualDownloadColumn.setCellValueFactory(cdf -> {
            DownloadTableRow downloadRow = cdf.getValue();
            Button btn = new Button("Manual download");
            btn.setOnAction(e -> manuallyDownload(downloadRow));
            // wrap it so it can be displayed in the TableView
            return new ReadOnlyObjectWrapper<>(btn);
        });

        tableView.getItems().addAll(this.rows);
        StackPane root = new StackPane();
        root.getChildren().add(tableView);
        window.setScene(new Scene(root, 450, 350));
        window.showAndWait();
    }



    public void manuallyDownload(DownloadTableRow downloadTableRow) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Download");
        String message = String.format("Manually download isopret-gui resource: %s", downloadTableRow.basename);
        alert.setHeaderText(message);
        alert.setContentText(String.format("URL: %s",downloadTableRow.url()));
        ButtonType systemDownloadButton = new ButtonType("Download with system browser");
        ButtonType clipboardButton = new ButtonType("Copy URL to system clipboard");
        ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(systemDownloadButton, clipboardButton, buttonTypeCancel);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == systemDownloadButton){
            hostServicesWrapper.showDocument(downloadTableRow.url());
            alert.close();
        } else {
            alert.close();
        }
    }



}
