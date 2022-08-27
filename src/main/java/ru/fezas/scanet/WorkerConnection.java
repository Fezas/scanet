package ru.fezas.scanet;

import javafx.collections.ObservableList;
import javafx.scene.paint.Color;
import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.controlsfx.glyphfont.GlyphFont;
import org.controlsfx.glyphfont.GlyphFontRegistry;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;

@Getter
@Setter
public class WorkerConnection extends Thread {
    private GlyphFont fontAwesome = GlyphFontRegistry.font("FontAwesome");
    private ScanetController scanetController = ScanetController.getInstance();
    private static final Logger logger = LogManager.getLogger();
    private boolean active = true;
    private Integer idWorker;
    private String nameWorker, ip;
    private Integer ping, timeUpdate;
    private String dateTimeLastPing;


    public WorkerConnection(boolean active, Integer id, String name,
                            String ip, Integer ping, Integer timeUpdate) {
        this.active = active;
        this.idWorker = id;
        this.nameWorker = name;
        this.ip = ip;
        this.ping = ping;
        this.timeUpdate = timeUpdate;
    }

    public WorkerConnection() {
    }
    private String timeOperation() {
        Instant instant = Instant.ofEpochMilli(System.currentTimeMillis());
        Date date = Date.from(instant);
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        String formattedDate = formatter.format(date);
        return formattedDate;
    }
    @Override
    public void run(){

        ObservableList<StationEntity> reportData = ReportController.data;
        try {
            while (!this.isInterrupted()) {
                while (active) {
                    Long startTime = System.currentTimeMillis();
                    InetAddress geek = InetAddress.getByName(ip);
                    System.out.println("Sending Ping Request to " + ip);
                    if (geek.isReachable(5000)) {
                        dateTimeLastPing = timeOperation();
                        ping = Math.toIntExact(System.currentTimeMillis() - startTime);
                        logger.info("INFO: пинг абонента " + nameWorker
                                + " IP: " + ip + " УСПЕШНО, PING: " + ping
                                + " " + dateTimeLastPing);
                        for (Integer i = 0; i < ReportController.data.size(); i++) {
                            if (ReportController.data.get(i).getId() == idWorker) {
                                StationEntity tmp = ReportController.data.get(i);
                                tmp.setPing(ping);
                                tmp.setTimeLastPing(dateTimeLastPing);
                                tmp.setInfo(fontAwesome.create("CIRCLE").color(Color.GREEN));
                                ReportController.data.set(i, tmp);
                                if (!scanetController.mapWork.containsKey(idWorker) ) {
                                    scanetController.mapWork.put(idWorker, ReportController.data.get(i));
                                    if (scanetController.mapErr.containsKey(idWorker)) scanetController.mapErr.remove(idWorker);
                                }
                            }
                        }
                    } else {
                        logger.info("INFO: пинг абонента " + nameWorker + " IP: " + ip + " ОШИБКА " + timeOperation());
                        dateTimeLastPing = timeOperation();
                        for (Integer i = 0; i < ReportController.data.size(); i++) {
                            if (ReportController.data.get(i).getId() == idWorker) {
                                StationEntity tmp = ReportController.data.get(i);
                                tmp.setPing(ping);
                                tmp.setTimeLastPing(dateTimeLastPing);
                                tmp.setInfo(fontAwesome.create("TIMES").color(Color.RED));
                                ReportController.data.set(i, tmp);
                                if (!scanetController.mapErr.containsKey(idWorker) ) {
                                    scanetController.mapErr.put(idWorker, ReportController.data.get(i));
                                    if (scanetController.mapWork.containsKey(idWorker)) scanetController.mapErr.remove(idWorker);
                                }

                            }
                        }
                    }
                    scanetController.countWorkAndErr();
                    sleep(timeUpdate * 1000);
                }
            }
        } catch (UnknownHostException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (InterruptedException e) {
            logger.info("INFO: сканер абонента " + nameWorker +  " остановлен " + timeOperation());
        }
    }
}
