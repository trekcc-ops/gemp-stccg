package com.gempukku.stccg.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;
import java.util.TreeSet;

public class DbIgnoreDAO implements IgnoreDAO {
    private static final String SELECT_STATEMENT = "select ignoredName from ignores where playerName=?";
    private static final String INSERT_STATEMENT = "insert into ignores (playerName, ignoredName) values (?, ?)";
    private static final String REMOVE_STATEMENT = "delete from ignores where playerName=? and ignoredName=?";
    private final DbAccess _dbAccess;

    public DbIgnoreDAO(DbAccess dbAccess) {
        _dbAccess = dbAccess;
    }

    @Override
    public final Set<String> getIgnoredUsers(String playerId) {
        try {
            try (Connection connection = _dbAccess.getDataSource().getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement(SELECT_STATEMENT)) {
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
    public final boolean addIgnoredUser(String playerId, String ignoredName) {
        try {
            SQLUtils.executeStatementWithParameters(_dbAccess, INSERT_STATEMENT, playerId, ignoredName);
            return true;
        } catch (SQLException exp) {
            throw new RuntimeException("Unable to add ignored user", exp);
        }
    }

    @Override
    public final boolean removeIgnoredUser(String playerId, String ignoredName) {
        try {
            SQLUtils.executeStatementWithParameters(_dbAccess, REMOVE_STATEMENT, playerId, ignoredName);
            return true;
        } catch (SQLException exp) {
            throw new RuntimeException("Unable to remove ignored user", exp);
        }
    }
}