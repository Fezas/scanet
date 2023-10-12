package ru.fezas.scanet.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.controlsfx.control.ToggleSwitch;
import ru.fezas.scanet.DAO.SettingsDAO;
import ru.fezas.scanet.entity.SettingsEntity;

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
    private TextField tfBadTime, tfGreatTime, tfGoodTime, tfReloadTime;

    @FXML
    private TextField tfTimeNotConnecting;

    @FXML
    void close() {
        Stage stage = (Stage) btnClose.getScene().getWindow();
        stage.close();
    }

    @FXML
    void save() {
        SettingsEntity settings = new SettingsEntity();
        settings.setId(0);
        if (selectSizeWindow.getSelectionModel().isSelected(0)) settings.setTypeWindow(true);
        else settings.setTypeWindow(false);
        settings.setGreat(Integer.parseInt(tfGreatTime.getText()));
        settings.setGood(Integer.parseInt(tfGoodTime.getText()));
        settings.setBad(Integer.parseInt(tfBadTime.getText()));
        settings.setTracing(swithPath.isSelected());
        SettingsDAO.getInstance().update(settings);
        close();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        selectSizeWindow.getItems().add("Минимальное");
        selectSizeWindow.getItems().add("Стандартное");
        selectSizeWindow.getSelectionModel().selectFirst();
        SettingsEntity settings = SettingsDAO.getInstance().find();
        if (settings.isTypeWindow()) {
            selectSizeWindow.getSelectionModel().selectFirst();
        }
        else selectSizeWindow.getSelectionModel().selectLast();
        tfGreatTime.setText(settings.getGreat().toString());
        tfGoodTime.setText(settings.getGood().toString());
        tfBadTime.setText(settings.getBad().toString());
        swithPath.setSelected(settings.isTracing());
    }
}
