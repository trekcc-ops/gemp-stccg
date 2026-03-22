package com.gempukku.stccg.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;

public class DbLeagueParticipationDAO implements LeagueParticipationDAO {
    private final DbAccess _dbAccess;

    public DbLeagueParticipationDAO(DbAccess dbAccess) {
        _dbAccess = dbAccess;
    }

    public final void userJoinsLeague(int leagueId, User player, String remoteAddress) {
        try {
            String sqlStatement = "insert into league_participation (league_id, player_name, join_ip) values (?,?,?)";
            SQLUtils.executeStatementWithParameters(_dbAccess, sqlStatement,
                    leagueId, player.getName(), remoteAddress);
        } catch (SQLException exp) {
            throw new RuntimeException(exp);
        }
    }

    @Override
    public final Collection<String> getUsersParticipating(int leagueId) {
        try {
            try (Connection conn = _dbAccess.getDataSource().getConnection()) {
                try (PreparedStatement statement = conn.prepareStatement(
                        "select player_name from league_participation where league_id=?")) {
                    statement.setInt(1, leagueId);
                    try (ResultSet rs = statement.executeQuery()) {
                        Collection<String> result = new HashSet<>();
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