package com.gempukku.stccg.database;

import com.gempukku.stccg.TextUtils;
import org.sql2o.Sql2o;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@SuppressWarnings("TrailingWhitespacesInTextBlock")
public class DbPlayerDAO implements PlayerDAO {
    private static final String SELECT_PLAYER = """
        SELECT 
            id, 
            name, 
            password, 
            type, 
            last_login_reward, 
            banned_until, 
            create_ip, 
            last_ip 
        FROM player
        """;


    private final DbAccess _dbAccess;

    public DbPlayerDAO(DbAccess dbAccess) {
        _dbAccess = dbAccess;
    }

    @Override
    public final User getPlayer(int id) {
        try {
            return getPlayerFromDBById(id);
        } catch (SQLException exp) {
            throw new RuntimeException("Error while retrieving player", exp);
        }
    }

    @Override
    public final User getPlayer(String playerName) throws UserNotFoundException {
        try {
            return getPlayerFromDBByName(playerName);
        } catch (SQLException exp) {
            throw new UserNotFoundException("Unable to find user '" + playerName + "' in database");
        }
    }

    @Override
    public final List<User> findSimilarAccounts(String login) throws SQLException {
        final User player = getPlayerFromDBByName(login);
        if (player == null)
            return new ArrayList<>();

        try (Connection conn = _dbAccess.getDataSource().getConnection()) {
            String sql = """
        SELECT 
            id, 
            name, 
            password, 
            type, 
            last_login_reward, 
            banned_until, 
            create_ip, 
            last_ip 
        FROM player
        """ + " where password=?";
            if (player.getCreateIp() != null)
                sql += " or create_ip=? or last_ip=?";
            if (player.getLastIp() != null)
                sql += " or create_ip=? or last_ip=?";

            try (PreparedStatement statement = conn.prepareStatement(sql)) {
                statement.setString(1, player.getPassword());
                int nextParamIndex = 2;
                if (player.getCreateIp() != null) {
                    statement.setString(nextParamIndex, player.getCreateIp());
                    statement.setString(nextParamIndex + 1, player.getCreateIp());
                    nextParamIndex += 2;
                }
                if (player.getLastIp() != null) {
                    statement.setString(nextParamIndex, player.getLastIp());
                    statement.setString(nextParamIndex + 1, player.getLastIp());
                }
                try (ResultSet rs = statement.executeQuery()) {
                    List<User> players = new LinkedList<>();
                    while (rs.next())
                        players.add(getPlayerFromResultSet(rs));
                    return players;
                }
            }
        }
    }

    @Override
    public final Set<String> getBannedUsernames() {
        try {
            try (Connection connection = _dbAccess.getDataSource().getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement(
                        "SELECT name FROM player WHERE type = '' ORDER BY ID DESC LIMIT 50")) {

                    try (ResultSet resultSet = statement.executeQuery()) {
                        Set<String> users = new TreeSet<>();
                        while (resultSet.next()) {
                            users.add(resultSet.getString(1));
                        }
                        return users;
                    }
                }
            }
        } catch (SQLException exp) {
            throw new RuntimeException("Unable to get banned users", exp);
        }
    }

    @Override
    public final boolean resetUserPassword(String login) {
        try {
            Sql2o db = new Sql2o(_dbAccess.getDataSource());

            try (org.sql2o.Connection conn = db.beginTransaction()) {
                String sql = """
                                UPDATE player
                                SET password = ''
                                WHERE name = :login
                            """;
                conn.createQuery(sql)
                        .addParameter("login", login)
                        .executeUpdate();

                conn.commit();

                return conn.getResult() == 1;
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to reset password", ex);
        }
    }

    @Override
    public final boolean banPlayerPermanently(String login) throws SQLException {
        String sqlStatement = "update player set type='', banned_until=null where name=?";
        return SQLUtils.executeUpdateStatementWithParameters(_dbAccess, sqlStatement, login);
    }

    @Override
    public final boolean banPlayerTemporarily(String login, long dateTo) throws SQLException {
        String sqlStatement = "update player set banned_until=?, type='un' where name=?";
        return SQLUtils.executeUpdateStatementWithParameters(_dbAccess, sqlStatement,
                dateTo, login);
    }

    @Override
    public final boolean unBanPlayer(String login) throws SQLException {
        String sqlStatement = "update player set type='un', banned_until=null where name=?";
        return SQLUtils.executeUpdateStatementWithParameters(_dbAccess, sqlStatement, login);
    }

    @Override
    public final boolean addPlayerFlag(String login, User.Type flag) {
        try {
            Sql2o db = new Sql2o(_dbAccess.getDataSource());

            try (org.sql2o.Connection conn = db.beginTransaction()) {
                String sql = """
                                UPDATE player
                                SET type = CONCAT(type, :type)
                                WHERE name= :login
                                    AND type NOT LIKE CONCAT('%', :type, '%');
                            """;
                conn.createQuery(sql)
                        .addParameter("login", login)
                        .addParameter("type", flag.getValue())
                        .executeUpdate();

                conn.commit();
                return conn.getResult() == 1;
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to update player with playtester flag", ex);
        }
    }

    @Override
    public final boolean removePlayerFlag(String login, User.Type flag) {
        try {
            Sql2o db = new Sql2o(_dbAccess.getDataSource());

            try (org.sql2o.Connection conn = db.beginTransaction()) {
                String sql = """
                                UPDATE player
                                SET type = REPLACE(type, :type, '')
                                WHERE name= :login
                                    AND type LIKE CONCAT('%', :type, '%');
                            """;
                conn.createQuery(sql)
                        .addParameter("login", login)
                        .addParameter("type", flag.getValue())
                        .executeUpdate();

                conn.commit();
                return conn.getResult() == 1;
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to update player to remove playtester flag", ex);
        }
    }

    @Override
    public final User loginUser(String login, String password) {

        try {
            Sql2o db = new Sql2o(_dbAccess.getDataSource());

            try (org.sql2o.Connection conn = db.open()) {
                String sql = SELECT_PLAYER +
                        """
                            WHERE name = :login
                                AND (password = :password OR password = '')
                        """;
                List<DBData.DBPlayer> result = conn.createQuery(sql)
                        .addParameter("login", login)
                        .addParameter("password", encodePassword(password))
                        .executeAndFetch(DBData.DBPlayer.class);

                var def = result.stream().findFirst().orElse(null);
                if(def == null)
                    return null;

                return new User(def);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to retrieve login entries", ex);
        }
    }

    private static User getPlayerFromResultSet(ResultSet rs) throws SQLException {
        int id = rs.getInt(1);
        String name = rs.getString(2);
        String password = rs.getString(3);
        String type = rs.getString(4);
        Integer lastLoginReward = rs.getInt(5);
        if (rs.wasNull())
            lastLoginReward = null;
        Long bannedUntilLong = rs.getLong(6);
        if (rs.wasNull())
            bannedUntilLong = null;

        Date bannedUntil = null;
        if (bannedUntilLong != null)
            bannedUntil = new Date(bannedUntilLong);
        String createIp = rs.getString(7);
        String lastIp = rs.getString(8);

        return new User(id, name, password, type, lastLoginReward, bannedUntil, createIp, lastIp);
    }

    @Override
    public final void setLastReward(User player, int currentReward) throws SQLException {
        String sqlStatement = "update player set last_login_reward =? where id=?";
        SQLUtils.executeStatementWithParameters(_dbAccess, sqlStatement,
                currentReward, player.getId());
        player.setLastLoginReward(currentReward);
    }

    @Override
    public final synchronized boolean updateLastReward(User player, int previousReward, int currentReward)
            throws SQLException {
        String sqlStatement = "update player set last_login_reward =? where id=? and last_login_reward=?";
        if (SQLUtils.executeUpdateStatementWithParameters(_dbAccess, sqlStatement,
                currentReward, player.getId(), previousReward)) {
            player.setLastLoginReward(currentReward);
            return true;
        }
        return false;
    }

    @Override
    public final synchronized boolean registerUser(String login, String password, String remoteAddress)
            throws LoginInvalidException {
        if (!validLoginName(login))
            return false;

        if(loginExists(login)) {
            if(!needsPasswordReset(login))
                return false;

            //Login exists but has a blank/null password, meaning this user is actually performing a password reset
            try {
                Sql2o db = new Sql2o(_dbAccess.getDataSource());

                try (org.sql2o.Connection conn = db.beginTransaction()) {
                    String sql = """
                                UPDATE player
                                SET password = :password
                                WHERE name = :login
                            """;
                    conn.createQuery(sql)
                            .addParameter("login", login)
                            .addParameter("password", encodePassword(password))
                            .executeUpdate();

                    conn.commit();
                    return conn.getResult() == 1;
                }
            } catch (Exception ex) {
                throw new RuntimeException("Unable to update password", ex);
            }
        }


        try {
            Sql2o db = new Sql2o(_dbAccess.getDataSource());

            try (org.sql2o.Connection conn = db.beginTransaction()) {
                String sql = """
                                INSERT INTO player (name, password, type, create_ip)
                                VALUES (:login, :password, :type, :create_ip)
                            """;
                conn.createQuery(sql)
                        .addParameter("login", login)
                        .addParameter("password", encodePassword(password))
                        .addParameter("type", User.Type.USER.toString())
                        .addParameter("create_ip", remoteAddress)
                        .executeUpdate();

                conn.commit();
                return conn.getResult() == 1;
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to insert new user", ex);
        }
    }

    private static boolean validLoginName(String login) throws LoginInvalidException {
        if (login.length() < 2 || login.length() > 30)
            throw new LoginInvalidException();
        for (int i = 0; i < login.length(); i++) {
            String validLoginChars = TextUtils.getAllCharacters(true,true);
            if (!validLoginChars.contains(String.valueOf(login.charAt(i))))
                throw new LoginInvalidException();
        }

        String lowerCase = login.toLowerCase();
        return !lowerCase.startsWith("admin") &&
                !lowerCase.startsWith("guest") && !lowerCase.startsWith("system") && !lowerCase.startsWith("bye");
    }

    private boolean loginExists(String login) {

        try {
            Sql2o db = new Sql2o(_dbAccess.getDataSource());

            try (org.sql2o.Connection conn = db.open()) {
                String sql = SELECT_PLAYER +
                        """
                            WHERE LOWER(name) = :login
                        """;
                List<DBData.DBPlayer> result = conn.createQuery(sql)
                        .addParameter("login", login.toLowerCase())
                        .executeAndFetch(DBData.DBPlayer.class);

                var def = result.stream().findFirst().orElse(null);
                return def != null;
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to retrieve password reset entries", ex);
        }
    }

    private boolean needsPasswordReset(String login) {
        try {
            Sql2o db = new Sql2o(_dbAccess.getDataSource());

            try (org.sql2o.Connection conn = db.open()) {
                String sql = SELECT_PLAYER +
                        """
                            WHERE LOWER(name) = :login
                                AND (password = '' OR password IS NULL)
                        """;
                List<DBData.DBPlayer> result = conn.createQuery(sql)
                        .addParameter("login", login.toLowerCase())
                        .executeAndFetch(DBData.DBPlayer.class);

                var def = result.stream().findFirst().orElse(null);
                return def != null;
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to retrieve password reset entries", ex);
        }
    }

    private static String encodePassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.reset();
            return convertToHexString(digest.digest(password.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String convertToHexString(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte aByte : bytes) {
            String hex = Integer.toHexString(0xFF & aByte);
            if (hex.length() == 1)
                hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    private User getPlayerFromDBById(int id) throws SQLException {
        try (Connection conn = _dbAccess.getDataSource().getConnection()) {
            try (PreparedStatement statement = conn.prepareStatement(SELECT_PLAYER + " where id=?")) {
                statement.setInt(1, id);
                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        return getPlayerFromResultSet(rs);
                    } else {
                        return null;
                    }
                }
            }
        }
    }

    private User getPlayerFromDBByName(String playerName) throws SQLException {
        try (Connection conn = _dbAccess.getDataSource().getConnection()) {
            try (PreparedStatement statement = conn.prepareStatement(SELECT_PLAYER + " where name=?")) {
                statement.setString(1, playerName);
                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        return getPlayerFromResultSet(rs);
                    } else {
                        return null;
                    }
                }
            }
        }
    }

    @Override
    public final void updateLastLoginIp(String login, String remoteAddress) throws SQLException {
        String sqlStatement = "update player set last_ip=? where name=?";
        SQLUtils.executeStatementWithParameters(_dbAccess, sqlStatement,
                remoteAddress, login);
    }

    @Override
    public final List<DBData.DBPlayer> getAllPlayers() {

        try {

            Sql2o db = new Sql2o(_dbAccess.getDataSource());

            try (org.sql2o.Connection conn = db.open()) {
                String sql = "SELECT id, name FROM player";

                return conn.createQuery(sql)
                        .executeAndFetch(DBData.DBPlayer.class);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to retrieve players", ex);
        }
    }

}