package ru.fezas.scanet;
import lombok.Getter;
import lombok.Setter;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;
import org.controlsfx.glyphfont.GlyphFont;
import org.controlsfx.glyphfont.GlyphFontRegistry;

@Getter
@Setter
public class StationEntity {
    private Integer id;
    private String name, ip;
    private Integer ping, timeUpdate;
    private boolean track, status;
    private Glyph info;
    private String timeLastPing;


    public StationEntity() {
    }

    public StationEntity(Integer id, String name, String ip,
                         Integer ping, Integer timeUpdate,
                         boolean track, boolean status,
                         Glyph info, String timeLastPing) {
        this.id = id;
        this.name = name;
        this.ip = ip;
        this.ping = ping;
        this.timeUpdate = timeUpdate;
        this.track = track;
        this.status = status;
        this.info = info;
        this.timeLastPing = timeLastPing;
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
                ", status=" + status +
                '}';
    }
}
