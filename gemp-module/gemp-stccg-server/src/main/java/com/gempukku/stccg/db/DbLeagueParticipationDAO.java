package com.gempukku.stccg.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class DbLeagueParticipationDAO implements LeagueParticipationDAO {
    private final DbAccess _dbAccess;

    public DbLeagueParticipationDAO(DbAccess dbAccess) {
        _dbAccess = dbAccess;
    }

    public void userJoinsLeague(String leagueId, User player, String remoteAddress) {
        try {
            String sqlStatement = "insert into league_participation (league_type, player_name, join_ip) values (?,?,?)";
            SQLUtils.executeStatementWithParameters(_dbAccess, sqlStatement,
                    leagueId, player.getName(), remoteAddress);
        } catch (SQLException exp) {
            throw new RuntimeException(exp);
        }
    }

    public Collection<String> getUsersParticipating(String leagueId) {
        try {
            try (Connection conn = _dbAccess.getDataSource().getConnection()) {
                try (PreparedStatement statement = conn.prepareStatement("select player_name from league_participation where league_type=?")) {
                    statement.setString(1, leagueId);
                    try (ResultSet rs = statement.executeQuery()) {
                        Set<String> result = new HashSet<>();
                        while (rs.next())
                            result.add(rs.getString(1));
                        return result;
                    }
                }
            }
        } catch (SQLException exp) {
            throw new RuntimeException(exp);
        }
    }
}