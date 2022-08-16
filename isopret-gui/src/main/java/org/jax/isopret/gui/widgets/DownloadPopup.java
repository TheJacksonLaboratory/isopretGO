package org.jax.isopret.gui.widgets;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import org.jax.isopret.gui.service.HostServicesWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

public class DownloadPopup {
    Logger LOGGER = LoggerFactory.getLogger(DownloadPopup.class);

    private final HostServicesWrapper hostServicesWrapper;

    private final String downloadDirectory;

    private ObservableList<DownloadTableRow> rows;

    public DownloadPopup(Map<String, String> successulDownloads,
                         Map<String, String> failedDownloads,
                         String downloadDir,
                         HostServicesWrapper hostServicesWrapper) {
        this.hostServicesWrapper = hostServicesWrapper;
        this.downloadDirectory = downloadDir;
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
        window.setWidth(600);
        window.setOnCloseRequest(event -> window.close());
        window.setTitle(windowTitle);
        VBox vbox = new VBox();
        vbox.setPadding(new Insets(5));
        Text text1 = new Text("If one or more downloads fail, this dialog can be used to download them. ");
        Text text2 = new Text("All files must be downloaded to the same directory. ");
        Text text3;
        if (this.downloadDirectory == null) {
            text3 = new Text("No directory is currently selected");
        } else {
             text3 = new Text("The currently selected directory is " + this.downloadDirectory);
        }
        TextFlow textFlow = new TextFlow(text1, text2, text3);
        vbox.getChildren().add(textFlow);
        vbox.getChildren().add(new Separator());


        TableView<DownloadTableRow> tableView = new TableView<>();
        TableColumn<DownloadTableRow, String> nameColumn = new TableColumn<>("File");
        TableColumn<DownloadTableRow, String> successColumn = new TableColumn<>("Status");
        TableColumn<DownloadTableRow, Button> manualDownloadColumn = new TableColumn<>("Download");
        nameColumn.setPrefWidth(350);
        successColumn.setPrefWidth(60);
        manualDownloadColumn.setPrefWidth(120);


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


        manualDownloadColumn.setSortable(false);
        manualDownloadColumn.setCellValueFactory(cdf -> {
            DownloadTableRow downloadRow = cdf.getValue();
            Button btn = new Button("Manual download");
            btn.setOnAction(e -> manuallyDownload(downloadRow));
            // wrap it so it can be displayed in the TableView
            return new ReadOnlyObjectWrapper<>(btn);
        });
        tableView.getColumns().add(nameColumn);
        tableView.getColumns().add(successColumn);
        tableView.getColumns().add(manualDownloadColumn);
        tableView.getItems().addAll(this.rows);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY); // do not show "extra column"
        vbox.getChildren().add(tableView);
        StackPane root = new StackPane();
        root.getChildren().add(vbox);
        window.setScene(new Scene(root, 450, 350));
        window.showAndWait();
    }



    private void manuallyDownload(DownloadTableRow downloadTableRow) {
        LOGGER.error("Manually downloading {}: {}", downloadTableRow.basename(), downloadTableRow.url());
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Download");
        String message = String.format("Manually download isopret-gui resource: %s", downloadTableRow.basename());
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
