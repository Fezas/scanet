package ru.fezas.scanet.entity;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;
import org.controlsfx.glyphfont.GlyphFont;
import org.controlsfx.glyphfont.GlyphFontRegistry;
import org.kordamp.ikonli.javafx.FontIcon;

@Data
public class StationEntity {
    private Integer id;
    private String name, ip;
    private Integer ping, timeUpdate;
    private boolean track;
    private boolean worked;
    private boolean station;
    private FontIcon info;
    private String timeLastPing;
    private Button btnEdit;
    private Button btnDelete;
    private boolean node; //true - конечный абонент, false - узел трассировки


    public StationEntity() {
        btnEdit = new Button();
        btnEdit.setGraphic(new FontIcon("anto-edit"));
        btnEdit.setVisible(false);

        btnDelete = new Button();
        btnDelete.setGraphic(new FontIcon("anto-delete"));
        btnDelete.setVisible(false);
    }

    @Override
    public String toString() {
        return "StationEntity{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", ip='" + ip + '\'' +
                ", ping=" + ping +
                ", timeUpdate=" + timeUpdate +
                ", track=" + track +
                '}';
    }
}
