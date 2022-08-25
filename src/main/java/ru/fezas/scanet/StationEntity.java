package ru.fezas.scanet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StationEntity {
    private Integer id;
    private String name, ip, info;
    private Integer ping, timeUpdate;
    private boolean track, status;


    public StationEntity() {
    }

    public StationEntity(Integer id, String name, String ip, Integer ping, Integer timeUpdate, boolean track, boolean status, String info) {
        this.id = id;
        this.name = name;
        this.ip = ip;
        this.ping = ping;
        this.timeUpdate = timeUpdate;
        this.track = track;
        this.status = status;
        this.info = info;
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
