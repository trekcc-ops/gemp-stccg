package com.gempukku.stccg.db;

import com.gempukku.stccg.competitive.LeagueMatchResult;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;

public class DbLeagueMatchDAO implements LeagueMatchDAO {
    private final DbAccess _dbAccess;

    public DbLeagueMatchDAO(DbAccess dbAccess) {
        _dbAccess = dbAccess;
    }

    @Override
    public final Collection<LeagueMatchResult> getLeagueMatches(String leagueId) {
        try {
            try (Connection conn = _dbAccess.getDataSource().getConnection()) {
                try (PreparedStatement statement = conn.prepareStatement("select winner, loser, season_type from league_match where league_type=?")) {
                    statement.setString(1, leagueId);
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
    public final void addPlayedMatch(String leagueId, String seriesId, String winner, String loser) {
        try {
            String sqlStatement =
                    "insert into league_match (league_type, season_type, winner, loser) values (?, ?, ?, ?)";
            SQLUtils.executeStatementWithParameters(_dbAccess, sqlStatement,
                    leagueId, seriesId, winner, loser);
        } catch (SQLException exp) {
            throw new RuntimeException(exp);
        }
    }
}