package com.gempukku.stccg.db;

import com.gempukku.stccg.db.vo.League;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DbLeagueDAO implements LeagueDAO {
    private final DbAccess _dbAccess;

    public DbLeagueDAO(DbAccess dbAccess) {
        _dbAccess = dbAccess;
    }

    public void addLeague(int cost, String name, String type, String clazz, String parameters, int start, int endTime) {
        try {
            String sqlStatement = "insert into league (name, type, class, parameters, start, end, status, cost) values (?, ?, ?, ?, ?, ?, ?, ?)";
            SQLUtils.executeStatementWithParameters(_dbAccess, sqlStatement,
                    name, type, clazz, parameters, start, endTime, 0, cost);
        } catch(SQLException exp) {
            throw new RuntimeException("Unable to add league into DB", exp);
        }
    }

    public List<League> loadActiveLeagues(int currentTime) throws SQLException {
        try (Connection conn = _dbAccess.getDataSource().getConnection()) {
            try (PreparedStatement statement = conn.prepareStatement("select name, type, class, parameters, status, cost from league where end>=? order by start desc")) {
                statement.setInt(1, currentTime);
                try (ResultSet rs = statement.executeQuery()) {
                    List<League> activeLeagues = new ArrayList<>();
                    while (rs.next()) {
                        String name = rs.getString(1);
                        String type = rs.getString(2);
                        String clazz = rs.getString(3);
                        String parameters = rs.getString(4);
                        int status = rs.getInt(5);
                        int cost = rs.getInt(6);
                        activeLeagues.add(new League(cost, name, type, clazz, parameters, status));
                    }
                    return activeLeagues;
                }
            }
        }
    }

    public void setStatus(League league, int newStatus) {
        try {
            String sqlStatement = "update league set status=? where type=?";
            SQLUtils.executeStatementWithParameters(_dbAccess, sqlStatement,
                    newStatus, league.getType());
        } catch (SQLException exp) {
            throw new RuntimeException("Unable to update league status", exp);
        }
    }
}