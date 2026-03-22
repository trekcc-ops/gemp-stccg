package com.gempukku.stccg.database;

import com.gempukku.stccg.competitive.LeagueMatchResult;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;

public class DbLeagueMatchDAO implements LeagueMatchDAO {
    private static final String SELECT_STATEMENT =
            "SELECT winner, loser, series_name FROM league_match where league_id=?";
    private final DbAccess _dbAccess;

    public DbLeagueMatchDAO(DbAccess dbAccess) {
        _dbAccess = dbAccess;
    }

    @Override
    public final Collection<LeagueMatchResult> getLeagueMatches(int leagueId) {
        try {
            try (Connection conn = _dbAccess.getDataSource().getConnection()) {
                try (PreparedStatement statement = conn.prepareStatement(SELECT_STATEMENT)) {
                    statement.setInt(1, leagueId);
                    try (ResultSet rs = statement.executeQuery()) {
                        Collection<LeagueMatchResult> result = new HashSet<>();
                        while (rs.next()) {
                            result.add(new LeagueMatchResult(
                                    rs.getString(3), rs.getString(1), rs.getString(2)));
                        }
                        return result;
                    }
                }
            }
        } catch (SQLException exp) {
            throw new RuntimeException(exp);
        }
    }

    @Override
    public final void addPlayedMatch(int leagueId, String seriesName, String winner, String loser) {
        try {
            String sqlStatement =
                    "INSERT INTO league_match (league_id, series_name, winner, loser) values (?, ?, ?, ?)";
            SQLUtils.executeStatementWithParameters(_dbAccess, sqlStatement, leagueId, seriesName, winner, loser);
        } catch (SQLException exp) {
            throw new RuntimeException(exp);
        }
    }

}