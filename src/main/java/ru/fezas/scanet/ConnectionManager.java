package ru.fezas.scanet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public final class ConnectionManager {
    private static final String PASSWORD_KEY = "db.password";
    private static final String USER_KEY = "db.login";
    private static final String URL_KEY = "db.url";
    private static final String POOL_SIZE_KEY = "db.pool.size";
    private static final Integer DEFAULT_POOL_SIZE = 5;
    private static BlockingQueue<Connection> pool;
    private static List<Connection> sourceConnections;
    private static final Logger logger = LogManager.getLogger();
    static {
        loadDriver();
        initConnectionPool();
    }
    private ConnectionManager() {
    }


    private static void initConnectionPool() {
        var poolSize = PropertiesUtilBD.get(POOL_SIZE_KEY);
        var size = poolSize == null ? DEFAULT_POOL_SIZE : Integer.parseInt(poolSize);
        pool = new ArrayBlockingQueue<>(size);



        for (int i = 0; i < size; i++) {
            var connection = open();
            var proxyConnection = (Connection)
                    Proxy.newProxyInstance(ConnectionManager.class.getClassLoader(), new Class[]{Connection.class},
                    (proxy, metod, args) -> metod.getName().equals("close")
                        ? pool.add((Connection) proxy)
                    :metod.invoke(connection, args));
            pool.add(proxyConnection);
        }
    }

    public static Connection get() {
        try {
            return pool.take();
        } catch (InterruptedException e) {
            logger.error("Error", e);
            throw new RuntimeException(e);
        }
    }


    private static void loadDriver() {
        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            logger.error("Error", e);
            throw new RuntimeException(e);
        }
    }

    private static Connection open() {
        try {
            return DriverManager.getConnection(
                    PropertiesUtilBD.get(URL_KEY),
                    PropertiesUtilBD.get(USER_KEY),
                    PropertiesUtilBD.get(PASSWORD_KEY)
            );

        } catch (SQLException e) {
            logger.error("Error", e);
            throw new RuntimeException(e);
        }
    }
    public static void closePool() {
        try {
            for (Connection sourceConnection : sourceConnections) {
                sourceConnection.close();
            }
        } catch (SQLException e) {
            logger.error("Error", e);
            throw new RuntimeException(e);
        }
    }
}
