package ru.fezas.scanet;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.controlsfx.control.ToggleSwitch;
import org.controlsfx.glyphfont.Glyph;
import org.controlsfx.glyphfont.GlyphFont;
import org.controlsfx.glyphfont.GlyphFontRegistry;

import java.net.URL;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;

public class ReportController implements Initializable {
    private GlyphFont fontAwesome = GlyphFontRegistry.font("FontAwesome");
    public static ObservableList<StationEntity> data = FXCollections.synchronizedObservableList(FXCollections.observableArrayList());
    private static ReportController instance;
    private StationEntity selectStation;
    private static final Logger logger = LogManager.getLogger();
    public static boolean flagWork = false;
    private static ArrayList<WorkerConnection> workerList;

    public static ReportController getInstance() {
        if (instance == null) {
            instance = new ReportController();
        }
        return instance;
    }

    @FXML    private TableView<StationEntity> tableStation = new TableView<>();
    @FXML    private Button btnAdd, btnReload, btnClose;
    @FXML    private ToggleSwitch switchWork;
    @FXML    private TableColumn<StationEntity, Integer> columnPing;
    @FXML    private TableColumn<StationEntity, String> columnName, columnIP, columnTrack;
    @FXML    private TableColumn<StationEntity, String> columnInfo, columnTimeUpdate;

    private void createScene(ActionEvent event, StationEntity station, String title) {
        try {
            AddStationController addStationController = new AddStationController(station);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("add-station.fxml"));
            loader.setController(addStationController);
            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(loader.load()));
            stage.initModality(Modality.APPLICATION_MODAL);
            //stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/telegra.png"))));
            stage.setResizable(false);
            stage.showAndWait();
        } catch (NullPointerException e) {
            logger.error("Error", e);
            System.exit(-1);
        } catch (Exception e) {
            logger.error("Error", e);
        }
    }

    @FXML
    void add(ActionEvent event) {
        createScene(event, null, "Добавить соединение");
    }

    @FXML
    void reload(ActionEvent event) {
        endWork();
        startWork();
    }

    private void startWork() {
        //запускаем соединения
        initWorkerList();
        for (WorkerConnection worker : workerList) {
            worker.setDaemon(true);
            worker.start();
        }
        //помечаем что запуск состоялся
        flagWork = true;
        btnReload.setDisable(false);
        btnAdd.setDisable(true);
        logger.info("INFO: start scanner " + System.currentTimeMillis());
        switchWork.setSelected(true);
    }

    private void endWork() {
        //прерываем соединения
        for (WorkerConnection worker : workerList) {
            worker.interrupt();
        }
        //помечаем что работа прекращена
        flagWork = false;
        btnReload.setDisable(true);
        btnAdd.setDisable(false);
        logger.info("INFO: stop scanner " + System.currentTimeMillis());
        switchWork.setSelected(false);
    }

    @FXML
    void actionToggleClick(MouseEvent event) {
        if (switchWork.selectedProperty().get()) {
            startWork();
        } else {
            endWork();
        }
    }

    @FXML
    void close(ActionEvent event) {
        Platform.exit(); // закрыть все окна
    }

    @FXML
    public void refreshTable() {
        initData();
        tableStation.refresh();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (!flagWork) initData();
        btnAdd.setGraphic(fontAwesome.create("PLUS"));
        btnReload.setGraphic(fontAwesome.create("REPEAT"));
        btnClose.setGraphic(fontAwesome.create("TIMES"));
        try {
            tableStation.setEditable(false);
            tableStation.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            tableStation.setRowFactory(
                    tableView -> {
                        //событие по двойному клику строки
                        final TableRow<StationEntity> row = new TableRow<>();
                        row.setOnMouseClicked(event -> {
                            if (event.getClickCount() == 2 && (! row.isEmpty()) ) {
                                StationEntity rowData = row.getItem();
                            }
                        });
                        //контексное меню
                        final ContextMenu rowMenu = new ContextMenu();

                        MenuItem editItem = new MenuItem("Редактировать");
                        MenuItem removeItem = new MenuItem("Удалить");
                        editItem.setOnAction(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent event) {
                                endWork();
                                selectStation = row.getItem();
                                createScene(event, selectStation, "Редактирование соединения");
                                selectStation = null;
                            }
                        });
                        removeItem.setOnAction(event -> {
                            Alert alertDelete = new Alert(Alert.AlertType.CONFIRMATION);
                            alertDelete.setTitle("Внимание");
                            alertDelete.setHeaderText("Удаление записи");
                            alertDelete.setContentText("Удалить запись: " + row.getItem().getName() + "?");
                            Optional<ButtonType> option = alertDelete.showAndWait();
                            if (option.get() == null) {

                            } else if (option.get() == ButtonType.OK) {
                                endWork();
                                StationDAO stationDAO = StationDAO.getInstance();
                                stationDAO.delete(row.getItem().getId());
                                tableStation.getItems().remove(row.getItem());
                                refreshTable();
                            } else if (option.get() == ButtonType.CANCEL) {

                            }
                        });
                        rowMenu.getItems().addAll(editItem, removeItem);
                        // only display context menu for non-empty rows:
                        row.contextMenuProperty().bind(
                                Bindings.when(row.emptyProperty())
                                        .then((ContextMenu) null)
                                        .otherwise(rowMenu));
                        return row;
                    }
            );
            // устанавливаем тип и значение которое должно хранится в колонке
            columnName.setCellValueFactory(new PropertyValueFactory<StationEntity, String>("name"));
            columnIP.setCellValueFactory(new PropertyValueFactory<StationEntity, String>("ip"));
            columnPing.setCellValueFactory(new PropertyValueFactory<StationEntity, Integer>("ping"));
            columnTimeUpdate.setCellValueFactory(new PropertyValueFactory<StationEntity, String>("timeLastPing"));
            columnInfo.setCellValueFactory(new PropertyValueFactory<StationEntity, String>("info"));

            // заполняем таблицу данными
            tableStation.setItems(data);
            //Элементы управления
            switchWork.setSelected(flagWork);
        } catch (Exception throwables) {
            throwables.printStackTrace();
        }
    }

    public void initData() {
        data.clear();
        Glyph glyph = null;
        var stations = StationDAO.getInstance().findAll();
        for (StationEntity station : stations) {
            if (station.isTrack() == false) {
                glyph = fontAwesome.create("STOP");
                glyph.color(Color.GRAY);
            }
            else {
                glyph = fontAwesome.create("PAUSE");
                glyph.color(Color.GRAY);
            }
            data.add(new StationEntity(
                    station.getId(),
                    station.getName(),
                    station.getIp(),
                    0,
                    station.getTimeUpdate(),
                    station.isTrack(),
                    station.isStatus(),
                    glyph,
                    ""
            ));
        }
    }

    public void initWorkerList() {
        //создаем список только тех соединений которые будем отслеживать
        workerList = new ArrayList<>();
        for (StationEntity item : data) {
            if (item.isTrack()) {
                WorkerConnection workerConnection = new WorkerConnection(
                        true,
                        item.getId(),
                        item.getName(),
                        item.getIp(),
                        0,
                        item.getTimeUpdate()
                );
                workerList.add(workerConnection);
            }
        }
    }
}
