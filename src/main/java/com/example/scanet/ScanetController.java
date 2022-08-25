package com.example.scanet;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import org.controlsfx.control.ToggleSwitch;
import org.controlsfx.glyphfont.GlyphFont;
import org.controlsfx.glyphfont.GlyphFontRegistry;

import java.net.URL;
import java.util.ResourceBundle;

public class ScanetController implements Initializable {
    private GlyphFont fontAwesome = GlyphFontRegistry.font("FontAwesome");
    @FXML
    private Button btnClose, btnEdit;

    @FXML
    private ToggleSwitch switchWork;

    @FXML
    void close(ActionEvent event) {
        Platform.exit(); // закрыть все окна
    }

    @FXML
    void edit(ActionEvent event) {

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        btnEdit.setGraphic(fontAwesome.create("GEARS"));
        btnClose.setGraphic(fontAwesome.create("CLOSE"));
    }
}