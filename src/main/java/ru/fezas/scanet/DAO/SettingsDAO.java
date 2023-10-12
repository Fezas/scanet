package ru.fezas.scanet.DAO;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.fezas.scanet.ConnectionManager;
import ru.fezas.scanet.entity.SettingsEntity;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SettingsDAO {
    private static final SettingsDAO INSTANCE = new SettingsDAO();
    private static final Logger logger = LogManager.getLogger();
    private static final String FIND = """
            SELECT id,
                type_window,
                great,
                good,
                bad,
                tracing
            FROM settings
            WHERE id = 0
            """;

    private static final String UPDATE_SQL = """
            UPDATE settings
            SET type_window = ?,
            great = ?,
            good = ?,
            bad = ?,
            tracing = ?
            WHERE id = ?
            """;
    private SettingsDAO() {
    }
    public static SettingsDAO getInstance() {
        return INSTANCE;
    }

    public void update(SettingsEntity setting) {
        try (var connection = ConnectionManager.get();
             var preparedStatement = connection.prepareStatement(UPDATE_SQL)) {
            preparedStatement.setBoolean(1, setting.isTypeWindow());
            preparedStatement.setInt(2, setting.getGreat());
            preparedStatement.setInt(3, setting.getGood());
            preparedStatement.setInt(4, setting.getBad());
            preparedStatement.setBoolean(5, setting.isTracing());
            preparedStatement.setInt(6, setting.getId());
            preparedStatement.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            logger.error("ERROR: ", throwables);
        }
    }

    public SettingsEntity find() {
        try (var connection = ConnectionManager.get();
             var preparedStatement = connection.prepareStatement(FIND)) {
            var resultSet = preparedStatement.executeQuery();
            List<SettingsEntity> settings = new ArrayList<>();
            while (resultSet.next()) {
                settings.add(buildStation(resultSet));
            }
            return settings.get(0);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            logger.error("ERROR: ", throwables);
        }
        return null;
    }

    private SettingsEntity buildStation(ResultSet resultSet) throws SQLException {
        SettingsEntity entity = new SettingsEntity();
        entity.setId(resultSet.getInt("id"));
        entity.setTypeWindow(resultSet.getBoolean("type_window"));
        entity.setGreat(resultSet.getInt("great"));
        entity.setGood(resultSet.getInt("good"));
        entity.setBad(resultSet.getInt("bad"));
        entity.setTracing(resultSet.getBoolean("tracing"));
        return entity;
    }
}
