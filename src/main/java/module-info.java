module ru.fezas.scanet {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;
    requires lombok;
    requires org.apache.logging.log4j;
    requires java.sql;
    requires java.desktop;

    opens ru.fezas.scanet to javafx.fxml;

    exports ru.fezas.scanet;
}