package ru.fezas.scanet;

import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.controlsfx.control.ToggleSwitch;
import org.controlsfx.glyphfont.GlyphFont;
import org.controlsfx.glyphfont.GlyphFontRegistry;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextFormatter.Change;
import ru.fezas.scanet.StationDAO;
import ru.fezas.scanet.StationEntity;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

public class AddStationController implements Initializable {
    private StationEntity currentStation;
    private GlyphFont fontAwesome = GlyphFontRegistry.font("FontAwesome");
    @FXML    private Button btnClose, btnSave;
    @FXML    private ToggleSwitch switchStatus, switchTrack;
    @FXML    private TextField textfieldIP, textfieldName, textfieldTimeUpdate;
    public AddStationController(StationEntity currentStation) {
        this.currentStation = currentStation;
    }
    @FXML
    void close(ActionEvent event) {
        Stage stage = (Stage) btnClose.getScene().getWindow();
        stage.close();
    }

    @FXML
    void save(ActionEvent event) {
        StationDAO stationDAO = StationDAO.getInstance();
        StationEntity station = new StationEntity();
        station.setName(textfieldName.getText());
        station.setIp(textfieldIP.getText());
        station.setTimeUpdate(Integer.parseInt(textfieldTimeUpdate.getText()));
        station.setTrack(switchTrack.isSelected());
        station.setStatus(switchStatus.isSelected());
        station.setPing(0);
        if (currentStation != null) {//если это редактирование адреса
            station.setId(currentStation.getId());
            stationDAO.update(station);
        } else {
            stationDAO.save(station);
        }
        ReportController reportController = ReportController.getInstance();
        reportController.refreshTable();
        Stage stage = (Stage) btnSave.getScene().getWindow();
        stage.close();
    }

    private String makePartialIPRegex() {
        String partialBlock = "(([01]?[0-9]{0,2})|(2[0-4][0-9])|(25[0-5]))";
        String subsequentPartialBlock = "(\\."+partialBlock+")" ;
        String ipAddress = partialBlock+"?"+subsequentPartialBlock+"{0,3}";
        return "^"+ipAddress;
    }
    public void textFieldLenght(TextField textField, int limit) {
        Pattern pattern = Pattern.compile(".{0," + limit + "}");
        TextFormatter formatter = new TextFormatter((UnaryOperator<TextFormatter.Change>) change -> {
            return pattern.matcher(change.getControlNewText()).matches() ? change : null;
        });
        textField.setTextFormatter(formatter);
        //первый символ не может быть пробелом
        textField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (newValue.trim().length() == 0) {
                    textField.setText("");
                }
            }
        });
    }
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        //валидация поля на превышение и первый пробел
        textFieldLenght(textfieldName, 18);
        //валидация поля ip
        String regex = makePartialIPRegex();
        final UnaryOperator<Change> ipAddressFilter = c -> {
            String text = c.getControlNewText();
            if  (text.matches(regex)) {
                return c;
            } else {
                return null;
            }
        };
        //валидация заполенения полей формы
        btnSave.disableProperty().bind(
                Bindings.isEmpty(textfieldName.textProperty())
                        .or(Bindings.isEmpty(textfieldIP.textProperty()))
                        .or(Bindings.isEmpty(textfieldTimeUpdate.textProperty()))
        );
        if (currentStation != null) {//если это редактирование адреса
            textfieldName.setText(currentStation.getName());
            textfieldIP.setText(currentStation.getIp());
            textfieldTimeUpdate.setText(currentStation.getTimeUpdate().toString());
            if (currentStation.isStatus()) switchStatus.setSelected(true);
            if (currentStation.isTrack()) switchTrack.setSelected(true);
        }
        textfieldIP.setTextFormatter(new TextFormatter<>(ipAddressFilter));
        btnSave.setGraphic(fontAwesome.create("SAVE"));
    }
}
