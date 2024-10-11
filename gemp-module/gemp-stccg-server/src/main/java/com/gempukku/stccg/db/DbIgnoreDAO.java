package com.gempukku.stccg.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;
import java.util.TreeSet;

public class DbIgnoreDAO implements IgnoreDAO {
    private final DbAccess _dbAccess;

    public DbIgnoreDAO(DbAccess dbAccess) {
        _dbAccess = dbAccess;
    }

    @Override
    public Set<String> getIgnoredUsers(String playerId) {
        try {
            try (Connection connection = _dbAccess.getDataSource().getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement("select ignoredName from ignores where playerName=?")) {
                    statement.setString(1, playerId);

                    try (ResultSet resultSet = statement.executeQuery()) {
                        Set<String> ignoredUsers = new TreeSet<>();
                        while (resultSet.next()) {
                            ignoredUsers.add(resultSet.getString(1));
                        }
                        return ignoredUsers;
                    }
                }
            }
        } catch (SQLException exp) {
            throw new RuntimeException("Unable to get ignored users", exp);
        }
    }

    @Override
    public boolean addIgnoredUser(String playerId, String ignoredName) {
        try {
            String sqlStatement = "insert into ignores (playerName, ignoredName) values (?, ?)";
            SQLUtils.executeStatementWithParameters(_dbAccess, sqlStatement,
                    playerId, ignoredName);
            return true;
        } catch (SQLException exp) {
            throw new RuntimeException("Unable to add ignored user", exp);
        }
    }

    @Override
    public boolean removeIgnoredUser(String playerId, String ignoredName) {
        try {
            String sqlStatement = "delete from ignores where playerName=? and ignoredName=?";
            SQLUtils.executeStatementWithParameters(_dbAccess, sqlStatement,
                    playerId, ignoredName);
            return true;
        } catch (SQLException exp) {
            throw new RuntimeException("Unable to remove ignored user", exp);
        }
    }
}
