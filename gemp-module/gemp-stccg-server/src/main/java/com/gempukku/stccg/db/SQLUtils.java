package com.gempukku.stccg.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

public final class SQLUtils {

    private static void addParametersToStatement(PreparedStatement statement, Object... params) throws SQLException {
        int i = 0;
        for (Object param : params) {
            i++;
            switch (param) {
                case null -> statement.setString(i, null);
                case String paramString -> statement.setString(i, paramString);
                case Boolean paramBool -> statement.setInt(i, paramBool ? 1 : 0);
                case Float paramFloat -> statement.setFloat(i, paramFloat);
                case Timestamp paramTimestamp -> statement.setTimestamp(i, paramTimestamp);
                case Long paramLong -> statement.setLong(i, paramLong);
                case Integer paramInt -> statement.setInt(i, paramInt);
                default -> throw new SQLException("Statement parameters need to be integer, long, string, or null");
            }
        }
    }

    public static void executeStatementWithParameters(DbAccess dbAccess, String queryString, Object... params)
            throws SQLException {
        try (Connection conn = dbAccess.getDataSource().getConnection()) {
            try (PreparedStatement statement = conn.prepareStatement(queryString)) {
                addParametersToStatement(statement, params);
                statement.execute();
            }
        }
    }

    public static boolean executeUpdateStatementWithParameters(DbAccess dbAccess, String sqlString, Object... params)
            throws SQLException {
        try (Connection conn = dbAccess.getDataSource().getConnection()) {
            try (PreparedStatement statement = conn.prepareStatement(sqlString)) {
                addParametersToStatement(statement, params);
                return statement.executeUpdate() == 1;
            }
        }
    }
}
