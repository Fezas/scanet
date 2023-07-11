package ru.fezas.scanet.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.controlsfx.control.ToggleSwitch;

import java.net.URL;
import java.util.ResourceBundle;

public class SettingController implements Initializable {
    @FXML
    private Button btnClose, btnSave;

    @FXML
    private ComboBox<String> selectSizeWindow;

    @FXML
    private ToggleSwitch swithPath;

    @FXML
    private TextField tfBadTime, tfCoolTime, tfGoodTime, tfReloadTime;

    @FXML
    private TextField tfTimeNotConnecting;

    @FXML
    void close(ActionEvent event) {
        Stage stage = (Stage) btnClose.getScene().getWindow();
        stage.close();
    }

    @FXML
    void save(ActionEvent event) {

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }
}
