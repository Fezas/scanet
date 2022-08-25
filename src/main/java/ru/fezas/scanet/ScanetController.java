package ru.fezas.scanet;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.controlsfx.control.ToggleSwitch;
import org.controlsfx.glyphfont.GlyphFont;
import org.controlsfx.glyphfont.GlyphFontRegistry;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public class ScanetController implements Initializable {
    private GlyphFont fontAwesome = GlyphFontRegistry.font("FontAwesome");
    private static double xOffset;
    private static double yOffset;
    @FXML
    private Button btnClose, btnEdit;

    @FXML
    private ToggleSwitch switchWork;

    @FXML
    void close(ActionEvent event) {
        Platform.exit(); // закрыть все окна
    }

    @FXML
    public void report(ActionEvent event) throws IOException {
        Stage stage = (Stage) btnClose.getScene().getWindow();
        stage.close();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("full-report.fxml"));
        Stage reportStage = new Stage();
        Scene scene = new Scene(loader.load());
        reportStage.setScene(scene);
        scene.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                xOffset = reportStage.getX() - event.getScreenX();
                yOffset = reportStage.getY() - event.getScreenY();
            }
        });
        scene.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                reportStage.setX(event.getScreenX() + xOffset);
                reportStage.setY(event.getScreenY() + yOffset);
            }
        });
        scene.setFill (Color.TRANSPARENT);
        reportStage.initModality(Modality.APPLICATION_MODAL);
        reportStage.initStyle(StageStyle.TRANSPARENT);
        String stylesheet = getClass().getResource("styles.css").toExternalForm();
        scene.getStylesheets().add(stylesheet);
        //reportStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/telegra.png"))));
        reportStage.setResizable(false);
        reportStage.showAndWait();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        btnEdit.setGraphic(fontAwesome.create("EYE"));
        btnClose.setGraphic(fontAwesome.create("CLOSE"));
    }
}