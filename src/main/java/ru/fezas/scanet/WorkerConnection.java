package ru.fezas.scanet;

import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.controlsfx.glyphfont.GlyphFont;
import org.controlsfx.glyphfont.GlyphFontRegistry;
import ru.fezas.scanet.controller.ReportController;
import ru.fezas.scanet.entity.StationEntity;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@Setter
public class WorkerConnection extends Thread {
    private GlyphFont fontAwesome = GlyphFontRegistry.font("FontAwesome");
    private final Logger logger = LogManager.getLogger();
    private boolean active = true;
    private Integer idWorker;
    private String nameWorker, ip;
    private Integer ping, timeUpdate;
    private String dateTimeLastPing;
    private Integer countHops; //количество хопов
    private Boolean success = false; //флаг рабочей сети
    private ArrayList<StationEntity> tracesEntity;
    private Process scan;
    public WorkerConnection(boolean active, Integer id, String name,
                            String ip, Integer timeUpdate) {
        this.active = active;
        this.idWorker = id;
        this.nameWorker = name;
        this.ip = ip;
        this.timeUpdate = timeUpdate;
    }

    public WorkerConnection() {
    }

    private String timeOperation() {
        Instant instant = Instant.ofEpochMilli(System.currentTimeMillis());
        Date date = Date.from(instant);
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM HH:mm:ss");
        String formattedDate = formatter.format(date);
        return formattedDate;
    }

    private void parser(ArrayList<String> traces, ArrayList<String> pingInfo) {
        tracesEntity = new ArrayList<>();
        String ipTrace = "";
        String nameTrace = "";
        Integer pingTrace = 0;
        for (String trace : traces) {
            trace = trace.replaceAll(" ms ", " "); //удаляем ед.измерения - она неизменна
            trace = trace.replaceAll("<", ""); //удаляем знак меньше - не актуален как показатель
            trace = trace.replaceAll("\\s+", " "); //удаляем лишние пробелы
            trace = trace.replaceAll("^\\s\\d+\\s", ""); //удаляем нумерацию
            trace = trace.replaceAll("\\*", "0"); //удаляем *
            System.out.println(trace);
            if (trace.substring(0, 15).equals("Transmit error:")) {
                System.out.println("ERROR");
            } else {
                Pattern patternFirstPing = Pattern.compile("^\\d+\\s\\d+\\s\\d+");
                Matcher matcherFirst = patternFirstPing.matcher(trace);
                pingTrace = 0;
                ipTrace = "";
                nameTrace = "";
                while (matcherFirst.find()) {
                    String[] subStr;
                    String delimeter = " "; // Разделитель
                    subStr = trace.substring(matcherFirst.start(), matcherFirst.end()).split(delimeter); // Разделения строки str с помощью метода split()
                    // Поиск наибольшего пинга в хопе
                    for(int i = 0; i < subStr.length; i++) {
                        if (pingTrace < Integer.parseInt(subStr[i])) pingTrace = Integer.parseInt(subStr[i]);
                    }
                }
                ipTrace = trace.replaceAll("^\\d+\\s\\d+\\s\\d+", "");
                ipTrace = ipTrace.trim();
                Pattern patternIp = Pattern.compile("\\[\\S+\\]");
                Matcher matcherIp = patternIp.matcher(ipTrace);
                while (matcherIp.find()) {
                    nameTrace = ipTrace.replaceAll("\\[\\S+\\]", "");
                    ipTrace = ipTrace.substring(matcherIp.start(), matcherIp.end());
                    ipTrace = ipTrace.replaceAll("[\\[\\]]", "");
                }

                //поиск наибольшего пинга среди хопов
                if (ping < pingTrace) ping = pingTrace;
                createTracesEntity(ipTrace, nameTrace, pingTrace); //записываем результаты парсинга в коллекцию
            }
        }
        if (success) {
            logger.info("INFO: пинг абонента " + nameWorker
                    + " IP: " + ip + " УСПЕШНО, PING: " + ping
                    + " " + dateTimeLastPing);
        } else {
            logger.info("INFO: пинг абонента " + nameWorker + " IP: " + ip + " ОШИБКА " + timeOperation());
        }
        for (String pinfo : pingInfo) {
            System.out.println(pinfo);
        }
    }

    private void createTracesEntity(String ipTrace, String nameTrace, Integer pingTrace) {
        StationEntity station = new StationEntity();
        station.setIp(ipTrace);
        if (!nameTrace.isEmpty()) station.setName(nameTrace);
        station.setPing(pingTrace);
        if (pingTrace > 0 && ipTrace.equals(ip)) {
            success = true;
        }
        tracesEntity.add(station);
    }

    private void runSystemCommand(String ip)
    {
        try {
            ArrayList<String> traces = new ArrayList<>();
            ArrayList<String> pingInfo = new ArrayList<>();

            //final URI uri = getClass().getResource("trace.bat").toURI();
            Map<String, String> env = new HashMap<>();
            env.put("create", "true");
            //FileSystem zipfs = FileSystems.newFileSystem(uri, env);
            //Path myFolderPath = Paths.get(uri);


            //URL res = getClass().getClassLoader().getResource("trace.bat");
            //File file = Paths.get(res.toURI()).toFile();
            //String absolutePath = file.getAbsolutePath();
            scan = Runtime.getRuntime()
                    .exec("cmd /c trace.bat" + " " + ip);
            InputStreamReader inputStreamReader = (new InputStreamReader(scan.getInputStream()));
            BufferedReader inputStream = new BufferedReader(inputStreamReader);
            String s = "";
            int count = 0;
            while((s = inputStream.readLine()) != null)
            {
                if (!s.isEmpty()) { //добавляем только не пустые строки
                    if (s.substring(0, 1).equals(" ")) { //добавляем только с строки с хопами
                        if (count > 1) {
                            traces.add(s); //первые две строки - это инфа по пингу
                        } else pingInfo.add(s);
                        count++;
                    }
                }
            }
            countHops = traces.size();
            parser(traces, pingInfo);
        }
        catch(Exception e)
        {
            e.printStackTrace();
            logger.error("ERROR: ", e);
        }
    }

    @Override
    public void run(){
        try {
            while (!this.isInterrupted()) {
                while (active) {
                    ping = 0;//сброс пинга
                    System.out.println("Sending Ping Request to " + ip);
                    runSystemCommand(ip);
                    ReportController.getInstance().addTraces(tracesEntity, idWorker, timeOperation(), success, ping);
                    sleep(timeUpdate * 1000);
                }
            }
        } catch (InterruptedException e) {
            logger.info("INFO: сканер абонента " + nameWorker +  " остановлен " + timeOperation());
        }
    }
}
