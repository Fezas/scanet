package com.example.scanet;

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
    private double xOffset;
    private double yOffset;
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
        Stage secondaryStage = new Stage();
        secondaryStage.initOwner(primaryStage);
        FXMLLoader fxmlLoader = new FXMLLoader(ScanetApplication.class.getResource("scanet.fxml"));
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
        secondaryStage.setTitle("Hello!");
        secondaryStage.setScene(scene);
        secondaryStage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}