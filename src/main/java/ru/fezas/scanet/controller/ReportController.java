package ru.fezas.scanet.controller;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.controlsfx.control.ToggleSwitch;
import org.controlsfx.glyphfont.GlyphFont;
import org.controlsfx.glyphfont.GlyphFontRegistry;
import org.kordamp.ikonli.javafx.FontIcon;
import ru.fezas.scanet.DAO.SettingsDAO;
import ru.fezas.scanet.DAO.StationDAO;
import ru.fezas.scanet.WorkerConnection;
import ru.fezas.scanet.entity.SettingsEntity;
import ru.fezas.scanet.entity.StationEntity;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class ReportController implements Initializable {
    private GlyphFont fontAwesome = GlyphFontRegistry.font("FontAwesome");
    private static final Logger logger = LogManager.getLogger();
    private static boolean flagWork = false;
    private static ArrayList<WorkerConnection> workerList;
    private static SettingsEntity settings;

    private static ReportController instance;
    public ReportController(){}
    public static synchronized ReportController getInstance() {
        if (instance == null) {
            instance = new ReportController();
        }
        return instance;
    }

    @FXML    private Button btnAdd, btnReload, btnSetting;
    @FXML    private ToggleSwitch switchWork;

    @FXML    private TreeTableView<StationEntity> tblReport;
    @FXML    private TreeTableColumn<StationEntity, String> clmnName, clmnIP, clmnInfo, clmnTimeUpdate, clmnEdit, clmnDelete;
    @FXML    private TreeTableColumn<StationEntity, Integer> clmnPing;



    private void createScene(StationEntity station, String title) {
        try {
            AddStationController addStationController = new AddStationController(station);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/add-station.fxml"));
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
    void add() {
        createScene(new StationEntity(), "Добавить соединение");
    }

    @FXML
    void edit(StationEntity entity) {
        createScene(entity, "Редактирование соединения");
    }

    @FXML
    void setting() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/setting.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Настройки");
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
    void reload() {
        if (flagWork) {
            endWork();
            startWork();
        } else tblReport.refresh();
    }


    private void startWork() {
        loadSettings();
        //запускаем соединения
        initWorkerList();
        for (WorkerConnection worker : workerList) {
            worker.setDaemon(true);
            worker.start();
        }
        flagWork = true;//флаг что запуск состоялся
        btnReload.setDisable(false);
        btnAdd.setDisable(true);
        btnSetting.setDisable(true);
        //btnLog.setDisable(true);
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
        btnSetting.setDisable(false);
        //btnLog.setDisable(false);
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
    void exit(ActionEvent event) {
        System.exit(0);
    }


    @FXML
    public void refreshTable() {
        if (flagWork) {
            endWork();
            createTreeItemTrackedNodes();
            startWork();
        } else createTreeItemTrackedNodes();
    }

    /**
     * Функция создания всплывающих подсказок {@link Tooltip} на объекты типа {@link Button}
     */
    private void tooltipButton (String title, Button btn) {
        Tooltip tooltip = new Tooltip();
        tooltip.setText(title);
        btn.setTooltip(tooltip);
    }



    /**
     * Функция формирования Action {@link Button}
     */
    private void addAction(StationEntity entity) {
        entity.getBtnEdit().setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                edit(entity);
            }
        });
        entity.getBtnDelete().setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                showAlertWithHeaderText(entity);
            }
        });
    }

    synchronized public void addTraces(ArrayList<StationEntity> traces, Integer idNode, String time, Boolean success, Integer ping) {
        ObservableList<TreeItem<StationEntity>> items = tblReport.getRoot().getChildren();
        for (TreeItem<StationEntity> treeItem : items) {
            if (treeItem.getValue().getId().equals(idNode)) {
                treeItem.getChildren().clear();
                FontIcon icon;
                if (success) {
                    icon = new FontIcon("anto-check:20");
                    if (ping <= settings.getGreat()) icon.setIconColor(Color.GREEN);
                    if (ping > settings.getGreat() && ping <= settings.getGood()) icon.setIconColor(Color.BLUE);
                    if (ping > settings.getGood() && ping <= settings.getBad()) icon.setIconColor(Color.BLUE);
                    treeItem.getValue().setTimeLastPing(time);
                    treeItem.getValue().setPing(ping);
                } else {
                    icon = new FontIcon("anto-close:20");
                    icon.setIconColor(Color.RED);
                }
                treeItem.getValue().setInfo(icon);
                if (traces.size() > 1) {
                    for (StationEntity trace : traces) {
                        TreeItem<StationEntity> traceItem = new TreeItem<StationEntity>(trace);
                        treeItem.getChildren().add(traceItem);
                    }
                }
                tblReport.refresh();
            }
        }
    }
    private void createTreeItemRoot() {
        StationEntity rootEntity = new StationEntity();
        rootEntity.setName("Структура");
        TreeItem<StationEntity> root = new TreeItem<StationEntity>(rootEntity);
        root.setExpanded(true);
        tblReport.setRoot(root);
    }

    private void createTreeItemTrackedNodes() {
        List<StationEntity> stations = StationDAO.getInstance().findAll();
        tblReport.getRoot().getChildren().clear();
        FontIcon icon;
        for (StationEntity station : stations) {
            station.setPing(0);
            station.setTimeLastPing("");
            tooltipButton("Корректировка узла \n\"" + station.getName() + "\"\n", station.getBtnEdit());
            tooltipButton("Удаление узла \n\"" + station.getName() + "\"\n", station.getBtnDelete());
            //иконки по умолчанию
            if (!station.isTrack()) {
                icon = new FontIcon("antf-close-square:16");
                icon.setIconColor(Color.GRAY);
            } else {
                icon = new FontIcon("anto-pause-circle:16");
                icon.setIconColor(Color.GRAY);
            }
            addAction(station);
            station.setInfo(icon);
            TreeItem<StationEntity> item = new TreeItem<StationEntity>(station);
            tblReport.getRoot().getChildren().add(item);
        }
    }

    private void loadSettings(){
        settings  = SettingsDAO.getInstance().find();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        btnAdd.setGraphic(new FontIcon("anto-plus:18"));
        btnReload.setGraphic(new FontIcon("anto-reload:18"));
        btnSetting.setGraphic(new FontIcon("anto-setting:18"));
        //btnLog.setGraphic(new FontIcon("anto-file-text:18"));
        tooltipButton("Добавить абенента", btnAdd);
        tooltipButton("Обновить данные", btnReload);
        tooltipButton("Настройки", btnSetting);
        //tooltipButton("Открыть логи", btnLog);
        loadSettings();
        createTreeItemRoot();
        createTreeItemTrackedNodes();
        try {
            tblReport.setRowFactory(
                    new Callback<TreeTableView<StationEntity>, TreeTableRow<StationEntity>>() {
                        @Override
                        public TreeTableRow<StationEntity> call(TreeTableView<StationEntity> tableView) {
                            final TreeTableRow<StationEntity> row = new TreeTableRow<StationEntity>();
                            row.setOnMouseEntered(event -> {
                                if (row.getTreeItem() != null) {
                                    if (row.getTreeItem().getValue().isNode() && !flagWork) {
                                        row.getTreeItem().getValue().getBtnEdit().setVisible(true);
                                        row.getTreeItem().getValue().getBtnDelete().setVisible(true);
                                    } else {
                                        row.getTreeItem().getValue().getBtnEdit().setVisible(false);
                                        row.getTreeItem().getValue().getBtnDelete().setVisible(false);
                                    }
                                }
                            });
                            row.setOnMouseExited(event -> {
                                if (row.getTreeItem() != null) {
                                    row.getTreeItem().getValue().getBtnEdit().setVisible(false);
                                    row.getTreeItem().getValue().getBtnDelete().setVisible(false);
                                }
                            });
                            return row;
                        }
                    }
            );
            // устанавливаем тип и значение которое должно хранится в колонке
            clmnName.setCellValueFactory(new TreeItemPropertyValueFactory<StationEntity, String>("name"));
            clmnIP.setCellValueFactory(new TreeItemPropertyValueFactory<StationEntity, String>("ip"));
            clmnPing.setCellValueFactory(new TreeItemPropertyValueFactory<StationEntity, Integer>("ping"));
            clmnTimeUpdate.setCellValueFactory(new TreeItemPropertyValueFactory<StationEntity, String>("timeLastPing"));
            clmnInfo.setCellValueFactory(new TreeItemPropertyValueFactory<StationEntity, String>("info"));
            clmnEdit.setCellValueFactory(new TreeItemPropertyValueFactory<StationEntity, String>("btnEdit"));
            clmnDelete.setCellValueFactory(new TreeItemPropertyValueFactory<StationEntity, String>("btnDelete"));
            //createStructure();
            //Элементы управления
            switchWork.setSelected(flagWork);
        } catch (Exception throwables) {
            throwables.printStackTrace();
            logger.error("Exception throwables ", throwables);
        }
    }

    public void initWorkerList() {
        //создаем список только тех соединений которые будем отслеживать
        workerList = new ArrayList<>();
        for (StationEntity item : StationDAO.getInstance().findAll()) {
            if (item.isTrack()) {
                WorkerConnection workerConnection = new WorkerConnection(
                        true,
                        item.getId(),
                        item.getName(),
                        item.getIp(),
                        item.getTimeUpdate()
                );
                workerList.add(workerConnection);
            }
        }
    }

    private void showAlertWithHeaderText(StationEntity entity) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Удаление записи");
        alert.setHeaderText("Внимание!");
        alert.setContentText("Вы действительно хотите удалить запись \"" + entity.getName());
        Optional<ButtonType> option = alert.showAndWait();
        if (option.get() == ButtonType.OK) {
            StationDAO.getInstance().delete(entity.getId());
            refreshTable();
        }
    }
}
