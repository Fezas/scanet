package ru.fezas.scanet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Properties;

public final class PropertiesApp {
    private static final Logger logger = LogManager.getLogger();
    private static final Properties PROPERTIESAPP = new Properties();
    static {
        loadPropertiesApp();
    }


    private PropertiesApp() {
    }

    public static String get(String key) {
        return PROPERTIESAPP.getProperty(key);
    }

    /**
     * Функция загрузки настроек
     */
    private static void loadPropertiesApp() {
        try (var inputStream = PropertiesApp.class.getClassLoader().getResourceAsStream("app.properties")) {
            PROPERTIESAPP.load(inputStream);
        } catch (IOException e) {
            logger.error("Error", e);
            throw new RuntimeException(e);
        }
    }
}
