package ru.fezas.scanet;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class ScanetApplication extends Application {
    private static ScanetApplication instance;
    private static Stage primaryStage;
    private static double xOffset;
    private static double yOffset;
    public static ScanetApplication getInstance() {
        if (instance == null) {
            instance = new ScanetApplication();
        }
        return instance;
    }
    @Override
    public void start(Stage primaryStage) throws IOException {
        //проставляем у главного окна стиль UTILITY
        //чтобы не отображалось в панели задач
        primaryStage.initStyle(StageStyle.UTILITY);
        //делаем главное окно полностью прозрачным
        primaryStage.setOpacity(0);
        //показываем
        primaryStage.show();
        //содержимое будет отражаться во втором, дочернем окне
        secondaryStage();
    }

    public static void secondaryStage() throws IOException {
        Stage secondaryStage = new Stage();
        secondaryStage.initOwner(primaryStage);
        FXMLLoader fxmlLoader = new FXMLLoader(ScanetApplication.class.getResource("scanet.fxml"));
        ScanetController scanetController = ScanetController.getInstance();
        fxmlLoader.setController(scanetController);
        Scene scene = new Scene(fxmlLoader.load());
        scene.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                xOffset = secondaryStage.getX() - event.getScreenX();
                yOffset = secondaryStage.getY() - event.getScreenY();
            }
        });
        scene.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                secondaryStage.setX(event.getScreenX() + xOffset);
                secondaryStage.setY(event.getScreenY() + yOffset);
            }
        });
        scene.setFill (Color.TRANSPARENT);
        secondaryStage.initStyle(StageStyle.TRANSPARENT);
        secondaryStage.setResizable(false);

        secondaryStage.setScene(scene);
        secondaryStage.show();
    }
    public static void main(String[] args) {
        launch();
    }
}