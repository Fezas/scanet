package ru.fezas.scanet;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StationDAO {
    private static final StationDAO INSTANCE = new StationDAO();
    private static final String DELETE_SQL = """
            DELETE FROM station
            WHERE id = ?
            """;
    private static final String SAVE_SQL = """
            INSERT INTO station (name, ip, ping, time_update, track, status) 
            VALUES (?, ?, ?, ?, ?, ?);
            """;
    private static final String FIND_ALL_SQL = """
            SELECT id,
                name,
                ip,
                ping,
                time_update,
                track,
                status,
            FROM station
            """;
    private static final String FIND_BY_ID_SQL = FIND_ALL_SQL + """
            WHERE id = ?
            """;
    private static final String UPDATE_SQL = """
            UPDATE station
            SET name = ?,
            ip = ?,
            ping = ?,
            time_update = ?,
            track = ?,
            status = ?
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
        }
        return false;
    }

    public StationEntity save(StationEntity station) {
        try (var connection = ConnectionManager.get();
             var preparedStatement = connection.prepareStatement(SAVE_SQL, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, station.getName());
            preparedStatement.setString(2, station.getIp());
            preparedStatement.setInt(3, station.getPing());
            preparedStatement.setInt(4, station.getTimeUpdate());
            preparedStatement.setBoolean(5, station.isTrack());
            preparedStatement.setBoolean(6, station.isStatus());
            preparedStatement.executeUpdate();
            var generatedKeys = preparedStatement.getGeneratedKeys();
            if (generatedKeys.next()) {
                station.setId(generatedKeys.getInt("id"));
            }
            return station;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }

    public void update(StationEntity station) {
        try (var connection = ConnectionManager.get();
             var preparedStatement = connection.prepareStatement(UPDATE_SQL)) {
            preparedStatement.setString(1, station.getName());
            preparedStatement.setString(2, station.getIp());
            preparedStatement.setInt(3, station.getPing());
            preparedStatement.setInt(4, station.getTimeUpdate());
            preparedStatement.setBoolean(5, station.isTrack());
            preparedStatement.setBoolean(6, station.isStatus());
            preparedStatement.setInt(7, station.getId());
            preparedStatement.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
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
        }
        return null;
    }

    private StationEntity buildStation(ResultSet resultSet) throws SQLException {
        return new StationEntity(
                resultSet.getInt("id"),
                resultSet.getString("name"),
                resultSet.getString("ip"),
                resultSet.getInt("ping"),
                resultSet.getInt("time_update"),
                resultSet.getBoolean("track"),
                resultSet.getBoolean("status"),
                ""
        );
    }
}