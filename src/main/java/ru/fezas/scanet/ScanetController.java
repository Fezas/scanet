package ru.fezas.scanet;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.controlsfx.glyphfont.Glyph;
import org.controlsfx.glyphfont.GlyphFont;
import org.controlsfx.glyphfont.GlyphFontRegistry;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

public class ScanetController implements Initializable {
    private static GlyphFont fontAwesome = GlyphFontRegistry.font("FontAwesome");
    public static Map<Integer, StationEntity> mapWork = new ConcurrentHashMap<>();
    public static Map<Integer, StationEntity> mapErr = new ConcurrentHashMap<>();

    public static ObservableList<ShortInfoScanner> info = FXCollections.synchronizedObservableList(FXCollections.observableArrayList());

    private static double xOffset;
    private static double yOffset;
    public static Glyph status = fontAwesome.create("PAUSE").size(20);
    private static ScanetController instance;
    public static ScanetController getInstance() {
        if (instance == null) {
            instance = new ScanetController();
        }
        return instance;
    }

    @FXML    private Button btnClose, btnEdit;
    @FXML    private Label labelInfo;
    @FXML    private TableColumn<ShortInfoScanner, Integer> columnErr;
    @FXML    private TableColumn<ShortInfoScanner, Integer> columnWork;
    @FXML    private TableView<ShortInfoScanner> tableShortInfo;


    @FXML
    void close(ActionEvent event) {
        Platform.exit(); // закрыть все окна
    }

    public synchronized static void countWorkAndErr() {
        ShortInfoScanner infoScanner = new ShortInfoScanner(mapWork.size(), mapErr.size());
        info.clear();
        info.add(infoScanner);
    }

    @FXML
    public void report(ActionEvent event) throws IOException {
        Stage stage = (Stage) btnClose.getScene().getWindow();
        stage.hide();
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
        reportStage.setResizable(false);
        reportStage.show();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        btnEdit.setGraphic(fontAwesome.create("EYE"));
        btnClose.setGraphic(fontAwesome.create("CLOSE"));
        labelInfo.setGraphic(status);
        columnWork.setCellValueFactory(new PropertyValueFactory<ShortInfoScanner, Integer>("workCount"));
        columnErr.setCellValueFactory(new PropertyValueFactory<ShortInfoScanner, Integer>("errCount"));
        tableShortInfo.setItems(info);
        tableShortInfo.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
    }
}