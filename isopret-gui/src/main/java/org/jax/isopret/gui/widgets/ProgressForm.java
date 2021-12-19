package org.jax.isopret.gui.widgets;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class ProgressForm {
    private final Stage dialogStage;
    private final ProgressBar pb = new ProgressBar();
    private final ProgressIndicator pin = new ProgressIndicator();
    private final Label titleLabel;
    private final Label messageLabel;

    public ProgressForm() {
        dialogStage = new Stage();
        dialogStage.initStyle(StageStyle.UTILITY);
        dialogStage.setResizable(false);
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        titleLabel = new Label("");
        messageLabel = new Label("");
        pb.setProgress(-1F);
        pin.setProgress(-1F);

        final HBox hb = new HBox();
        hb.setSpacing(5);
        hb.setAlignment(Pos.CENTER);
        hb.getChildren().addAll(pb, pin);
        final VBox vb = new VBox();
        vb.getChildren().addAll(titleLabel, hb, messageLabel);

        Scene scene = new Scene(vb);
        dialogStage.setScene(scene);
    }

    public void activateProgressBar(final Task<?> task)  {
        pb.progressProperty().bind(task.progressProperty());
        pin.progressProperty().bind(task.progressProperty());
        dialogStage.show();
        dialogStage.toFront();

    }

    public StringProperty titleProperty() {
        return titleLabel.textProperty();
    }

    public StringProperty messageProperty() {
        return messageLabel.textProperty();
    }

    public DoubleProperty progressProperty() { return pb.progressProperty(); }


    public void close() {
        dialogStage.close();
    }

    public Stage getDialogStage() {
        return dialogStage;
    }
}