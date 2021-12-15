package org.jax.isopret.gui.widgets;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.List;
import java.util.Optional;

public class PopupFactory {

    /**
     * Open up a dialog where the user can enter a new project name.
     * The function checks that the filename does not contain weird and potentially
     * invalid characters.
     *
     * @return String representing the chosen project name or null if the chosen name was invalid.
     */
    public static Optional<String> getProjectName() {
        String title = "Enter New Project Name";
        String labelText = "Enter project name:";
        String defaultProjectName = "new project";
        TextInputDialog dialog = new TextInputDialog(labelText);
        dialog.setTitle(title);
        dialog.setHeaderText(null);
        dialog.setContentText(labelText);
        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) {
            return result;
        }
        String answer = result.get();
        if (answer.matches(".*[]\\[!#$%&'()*+,/:;<=>?@^`{|}~].*")) {
            PopupFactory.displayError("File name error", "File name contains invalid characters");
            return Optional.empty();
        } else {
            return result;
        }
    }

    /**
     * Request a String from user.
     *
     * @param windowTitle - Title of PopUp window
     * @param promptText  - Prompt of Text field (suggestion for user)
     * @param labelText   - Text of your request
     * @return String with user input
     */
    public static String getStringFromUser(String windowTitle, String promptText, String labelText) {
        TextInputDialog dialog = new TextInputDialog(promptText);
        dialog.setTitle(windowTitle);
        dialog.setHeaderText(null);
        dialog.setContentText(labelText);
        Optional<String> result = dialog.showAndWait();

        return result.orElse(null);
    }


    public static void displayError(String title, String message) {
        Alert al = new Alert(Alert.AlertType.ERROR);
        DialogPane dialogPane = al.getDialogPane();
        dialogPane.setHeaderText(title);
        dialogPane.setContentText(message);
        dialogPane.getChildren().stream().filter(node -> node instanceof Label).forEach(node -> ((Label) node).setMinHeight(Region.USE_PREF_SIZE));
        al.showAndWait();
    }


    public static void displayMessage(String title, String message) {
        Alert al = new Alert(Alert.AlertType.INFORMATION);
        DialogPane dialogPane = al.getDialogPane();
        dialogPane.setHeaderText(title);
        dialogPane.setContentText(message);
        dialogPane.getChildren().stream().filter(node -> node instanceof Label).forEach(node -> ((Label) node).setMinHeight(Region.USE_PREF_SIZE));
        al.showAndWait();
    }


    public static void displayException(String title, String message, Exception e) {
        TextArea textArea = new TextArea(e.toString());
        textArea.setEditable(false);
        textArea.setWrapText(true);
        Label label = new Label("The exception stacktrace was:");
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);
        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(label, 0, 0);
        expContent.add(textArea, 0, 1);
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Exception Dialog");
        alert.setHeaderText(title);
        alert.setContentText(message);
        // Set expandable Exception into the dialog pane.
        alert.getDialogPane().setExpandableContent(expContent);
        alert.showAndWait();
    }


    public static void showAbout(String versionString, String dateString) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("GOPHER");
        alert.setHeaderText(null);
        alert.setContentText(String.format("Version %s\nLast changed: %s", versionString, dateString));
        alert.showAndWait();
    }

    public static boolean confirmDialog(String title, String message) {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setContentText(message);
        Optional<Boolean> result = dialog.showAndWait();
        if (result.isEmpty()) return false;
        else  return result.get();
    }


    private static String getPreHTML(String text) {
        return String.format("<html><body><h1>GOPHER Report</h1><pre>%s</pre></body></html>", text);
    }

    public static void showReportListDialog(List<String> reportlist) {
        Stage window;
        String windowTitle = "GOPHER Report";
        window = new Stage();
        window.setOnCloseRequest(event -> window.close());
        window.setTitle(windowTitle);

        ListView<String> list = new ListView<>();

        ObservableList<String> items = FXCollections.observableArrayList(reportlist);
        list.setItems(items);
        list.setPrefWidth(450);
        list.setPrefHeight(350);
        list.setOrientation(Orientation.VERTICAL);

        StackPane root = new StackPane();
        root.getChildren().add(list);
        window.setScene(new Scene(root, 450, 350));
        window.showAndWait();
    }

    /**
     * Show information to user.
     *
     * @param text        - message text
     * @param windowTitle - Title of PopUp window
     */
    public static void showInfoMessage(String text, String windowTitle) {
        Alert al = new Alert(Alert.AlertType.INFORMATION);
        DialogPane dialogPane = al.getDialogPane();
        dialogPane.getChildren().stream().filter(node -> node instanceof Label).forEach(node -> ((Label) node).setMinHeight(Region.USE_PREF_SIZE));
        al.setTitle(windowTitle);
        al.setHeaderText(null);
        al.setContentText(text);
        al.showAndWait();
    }


}
