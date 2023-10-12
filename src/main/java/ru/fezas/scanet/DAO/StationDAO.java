package ru.fezas.scanet.DAO;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.controlsfx.glyphfont.GlyphFont;
import org.controlsfx.glyphfont.GlyphFontRegistry;
import org.kordamp.ikonli.javafx.FontIcon;
import ru.fezas.scanet.ConnectionManager;
import ru.fezas.scanet.entity.StationEntity;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StationDAO {
    private static final Logger logger = LogManager.getLogger();
    private static final StationDAO INSTANCE = new StationDAO();
    private static final String DELETE_SQL = """
            DELETE FROM station
            WHERE id = ?
            """;
    private static final String SAVE_SQL = """
            INSERT INTO station (name, ip, time_update, track) 
            VALUES (?, ?, ?, ?);
            """;
    private static final String FIND_ALL_SQL = """
            SELECT id,
                name,
                ip,
                time_update,
                track
            FROM station
            """;
    private static final String FIND_BY_ID_SQL = FIND_ALL_SQL + """
            WHERE id = ?
            """;
    private static final String UPDATE_SQL = """
            UPDATE station
            SET name = ?,
            ip = ?,
            time_update = ?,
            track = ?
            WHERE id = ?
            """;
    private StationDAO() {
    }
    public static StationDAO getInstance() {
        return INSTANCE;
    }

    public boolean delete(Integer id) {
        try (var connection = ConnectionManager.get();
             var preparedStatement = connection.prepareStatement(DELETE_SQL)) {
            preparedStatement.setInt(1, id);
            return preparedStatement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            logger.error("ERROR: ", e);
        }
        return false;
    }

    public StationEntity save(StationEntity station) {
        try (var connection = ConnectionManager.get();
             var preparedStatement = connection.prepareStatement(SAVE_SQL, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, station.getName());
            preparedStatement.setString(2, station.getIp());
            preparedStatement.setInt(3, station.getTimeUpdate());
            preparedStatement.setBoolean(4, station.isTrack());
            preparedStatement.executeUpdate();
            var generatedKeys = preparedStatement.getGeneratedKeys();
            if (generatedKeys.next()) {
                station.setId(generatedKeys.getInt("id"));
            }
            return station;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            logger.error("ERROR: ", throwables);
        }
        return null;
    }

    public void update(StationEntity station) {
        try (var connection = ConnectionManager.get();
             var preparedStatement = connection.prepareStatement(UPDATE_SQL)) {
            preparedStatement.setString(1, station.getName());
            preparedStatement.setString(2, station.getIp());
            preparedStatement.setInt(3, station.getTimeUpdate());
            preparedStatement.setBoolean(4, station.isTrack());
            preparedStatement.setInt(5, station.getId());
            preparedStatement.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            logger.error("ERROR: ", throwables);
        }
    }

    public Optional<StationEntity> findById(Integer id) {
        try (var connection = ConnectionManager.get();
             var preparedStatement = connection.prepareStatement(FIND_BY_ID_SQL)) {
            preparedStatement.setInt(1, id);
            var resultSet = preparedStatement.executeQuery();
            StationEntity station = null;
            if (resultSet.next()) {
                station = buildStation(resultSet);
            }
            return Optional.ofNullable(station);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            logger.error("ERROR: ", throwables);
        }
        return null;
    }

    public List<StationEntity> findAll() {
        try (var connection = ConnectionManager.get();
             var preparedStatement = connection.prepareStatement(FIND_ALL_SQL)) {
            var resultSet = preparedStatement.executeQuery();
            List<StationEntity> stations = new ArrayList<>();
            while (resultSet.next()) {
                stations.add(buildStation(resultSet));
            }
            return stations;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            logger.error("ERROR: ", throwables);
        }
        return null;
    }

    private StationEntity buildStation(ResultSet resultSet) throws SQLException {
        StationEntity entity = new StationEntity();
        entity.setId(resultSet.getInt("id"));
        entity.setName(resultSet.getString("name"));
        entity.setIp(resultSet.getString("ip"));
        entity.setPing(0);
        entity.setInfo(new FontIcon());
        entity.setTimeUpdate(resultSet.getInt("time_update"));
        entity.setTrack(resultSet.getBoolean("track"));
        entity.setNode(true);
        return entity;
    }
}
