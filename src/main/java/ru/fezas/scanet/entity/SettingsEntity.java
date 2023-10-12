package ru.fezas.scanet.entity;
import javafx.scene.control.Button;
import lombok.Data;
import org.kordamp.ikonli.javafx.FontIcon;

@Data
public class SettingsEntity {
    private Integer id;
    private boolean typeWindow;
    private Integer great, good, bad;
    private boolean tracing;


    public SettingsEntity() {
    }

    @Override
    public String toString() {
        return "SettingsEntity{" +
                "typeWindow=" + typeWindow +
                ", great=" + great +
                ", good=" + good +
                ", bad=" + bad +
                ", tracing=" + tracing +
                '}';
    }
}
