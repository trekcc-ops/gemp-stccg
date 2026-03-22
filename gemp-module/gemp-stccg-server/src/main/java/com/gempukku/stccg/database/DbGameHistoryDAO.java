package com.gempukku.stccg.database;

import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.common.JSONData;
import org.sql2o.Query;
import org.sql2o.Sql2o;

import java.net.HttpURLConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("LongLine")
public class DbGameHistoryDAO implements GameHistoryDAO {
    private final DbAccess _dbAccess;
    private final DateTimeFormatter _dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final DateTimeFormatter _dateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public DbGameHistoryDAO(DbAccess dbAccess) {
        _dbAccess = dbAccess;
    }

    @SuppressWarnings("TrailingWhitespacesInTextBlock")
    public final int addGameHistory(String winner, int winnerId, String loser, int loserId, String winReason, String loseReason, String winRecordingId, String loseRecordingId,
                                    String formatName, String tournament, String winnerDeckName, String loserDeckName, ZonedDateTime startDate, ZonedDateTime endDate, int version) {
        try {
            Sql2o db = new Sql2o(_dbAccess.getDataSource());

            String sql = """
                        INSERT INTO game_history (winner, winnerId, loser, loserId, win_reason, lose_reason, win_recording_id, lose_recording_id, 
                            format_name, tournament, winner_deck_name, loser_deck_name, start_date, end_date, replay_version)
                        VALUES (:winner, :winnerId, :loser, :loserId, :win_reason, :lose_reason, :win_recording_id, :lose_recording_id, 
                            :format_name, :tournament, :winner_deck_name, :loser_deck_name, :start_date, :end_date, :version)
                        """;

            try (org.sql2o.Connection conn = db.beginTransaction()) {
                Query query = conn.createQuery(sql, true);
                query.addParameter("winner", winner)
                        .addParameter("winnerId", winnerId)
                        .addParameter("loser", loser)
                        .addParameter("loserId", loserId)
                        .addParameter("win_reason", winReason)
                        .addParameter("lose_reason", loseReason)
                        .addParameter("win_recording_id", winRecordingId)
                        .addParameter("lose_recording_id", loseRecordingId)
                        .addParameter("format_name", formatName)
                        .addParameter("tournament", tournament)
                        .addParameter("winner_deck_name", winnerDeckName)
                        .addParameter("loser_deck_name", loserDeckName)
                        .addParameter("start_date", startDate.format(_dateTimeFormat))
                        .addParameter("end_date", endDate.format(_dateTimeFormat))
                        .addParameter("version", version);

                int id = query.executeUpdate()
                        .getKey(Integer.class);
                conn.commit();

                return id;
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to insert game history", ex);
        }
    }

    @SuppressWarnings("TrailingWhitespacesInTextBlock")
    public final List<DBData.GameHistory> getGameHistoryForPlayer(User player, int start, int count) throws HttpProcessingException {

        try {

            Sql2o db = new Sql2o(_dbAccess.getDataSource());

            try (org.sql2o.Connection conn = db.open()) {
                String sql = """
                    SELECT winner, winnerId, loser, loserId, win_reason, lose_reason, win_recording_id, lose_recording_id, 
                            format_name, tournament, winner_deck_name, loser_deck_name, start_date, end_date, replay_version
                    FROM game_history 
                    WHERE winner = :playerName 
                        OR loser = :playerName 
                    ORDER BY end_date DESC
                    LIMIT :start, :count;
                """;

                return conn.createQuery(sql)
                        .addParameter("playerName", player.getName())
                        .addParameter("start", start)
                        .addParameter("count", count)
                        .executeAndFetch(DBData.GameHistory.class);
            }
        } catch (Exception ex) {
            throw new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND);
        }

    }

    @SuppressWarnings("TrailingWhitespacesInTextBlock")
    public final DBData.GameHistory getGameHistory(String recordID) {
        try {

            Sql2o db = new Sql2o(_dbAccess.getDataSource());

            try (org.sql2o.Connection conn = db.open()) {
                String sql = """
                    SELECT winner, winnerId, loser, loserId, win_reason, lose_reason, win_recording_id, lose_recording_id, 
                            format_name, tournament, winner_deck_name, loser_deck_name, start_date, end_date, replay_version 
                    FROM game_history 
                    WHERE win_recording_id = :recordID 
                        OR lose_recording_id = :recordID;
                """;
                List<DBData.GameHistory> result = conn.createQuery(sql)
                        .addParameter("recordID", recordID)
                        .executeAndFetch(DBData.GameHistory.class);

                return result.stream().findFirst().orElse(null);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to retrieve game history for player", ex);
        }

    }

    public final boolean doesReplayIDExist(String id) {
        try {

            Sql2o db = new Sql2o(_dbAccess.getDataSource());

            try (org.sql2o.Connection conn = db.open()) {
                String sql = """
                        SELECT COUNT(*)
                        FROM game_history
                        WHERE win_recording_id = :id
                            OR lose_recording_id = :id
                        """;
                Integer result = conn.createQuery(sql)
                        .addParameter("id", id)
                        .executeScalar(Integer.class);

                return result >= 1;
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to retrieve existence of replay ID", ex);
        }
    }

    @SuppressWarnings("TrailingWhitespacesInTextBlock")
    @Override
    public final List<DBData.GameHistory> getGameHistoryForFormat(String format, int count)  {
        try {

            Sql2o db = new Sql2o(_dbAccess.getDataSource());

            try (org.sql2o.Connection conn = db.open()) {
                String sql = """
                        SELECT
                            winner, winnerId, loser, loserId, win_reason, lose_reason, win_recording_id, lose_recording_id, 
                            format_name, tournament, winner_deck_name, loser_deck_name, start_date, end_date, replay_version
                        FROM game_history
                        WHERE format_name LIKE :format
                        ORDER BY end_date DESC
                        LIMIT :count
                        """;

                return conn.createQuery(sql)
                        .addParameter("format", "%" + format + "%")
                        .addParameter("count", count)
                        .executeAndFetch(DBData.GameHistory.class);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to retrieve game history by format", ex);
        }
    }

    public final int getGameHistoryForPlayerCount(User player) {
        try {

            Sql2o db = new Sql2o(_dbAccess.getDataSource());

            try (org.sql2o.Connection conn = db.open()) {
                String sql = """
                        SELECT COUNT(*)
                        FROM game_history
                        WHERE winner = :player
                            OR loser = :player
                        """;
                Integer result = conn.createQuery(sql)
                        .addParameter("player", player.getName())
                        .executeScalar(Integer.class);

                return Objects.requireNonNullElse(result, -1);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to retrieve count of player games", ex);
        }
    }

    @SuppressWarnings("TrailingWhitespacesInTextBlock")
    public final int getActivePlayersCount(ZonedDateTime from, ZonedDateTime to) {
        try {
            Sql2o db = new Sql2o(_dbAccess.getDataSource());

            try (org.sql2o.Connection conn = db.open()) {
                String sql = """
                        SELECT COUNT(*) 
                        FROM 
                        (
                            SELECT winner 
                            FROM game_history 
                            WHERE end_date BETWEEN :from AND :to
                             
                            UNION 
                            
                            SELECT loser 
                            FROM game_history 
                            WHERE end_date BETWEEN :from AND :to
                        ) AS U
                        """;
                Integer result = conn.createQuery(sql)
                        .addParameter("from", from.format(_dateFormat))
                        .addParameter("to", to.format(_dateFormat))
                        .executeScalar(Integer.class);

                return Objects.requireNonNullElse(result, -1);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to retrieve count of active players", ex);
        }
    }

    @SuppressWarnings("TrailingWhitespacesInTextBlock")
    public final int getGamesPlayedCount(ZonedDateTime from, ZonedDateTime to) {
        try {
            Sql2o db = new Sql2o(_dbAccess.getDataSource());

            try (org.sql2o.Connection conn = db.open()) {
                String sql = """
                        SELECT COUNT(*) 
                        FROM game_history 
                        WHERE end_date BETWEEN :from AND :to;
                        """;
                Integer result = conn.createQuery(sql)
                        .addParameter("from", from.format(_dateFormat))
                        .addParameter("to", to.format(_dateFormat))
                        .executeScalar(Integer.class);

                return Objects.requireNonNullElse(result, -1);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to retrieve count of games played", ex);
        }
    }

    public final List<JSONData.FormatStats> GetAllGameFormatData(ZonedDateTime from, ZonedDateTime to) {
        try {
            Sql2o db = new Sql2o(_dbAccess.getDataSource());

            try (org.sql2o.Connection conn = db.open()) {
                String sql = """
                        SELECT
                        	 COUNT(*) AS Count
                        	,format_name AS Format
                        	,CASE WHEN tournament IS NULL OR tournament LIKE 'Casual %' THEN 1 ELSE 0 END AS Casual
                        FROM game_history
                        WHERE end_date BETWEEN :from AND :to
                        GROUP BY format_name, CASE WHEN tournament IS NULL OR tournament LIKE 'Casual %' THEN 1 ELSE 0 END
                        """;

                return conn.createQuery(sql)
                        .addParameter("from", from.format(_dateFormat))
                        .addParameter("to", to.format(_dateFormat))
                        .executeAndFetch(JSONData.FormatStats.class);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to retrieve format stats", ex);
        }
    }


    public final List<PlayerStatistic> getCasualPlayerStatistics(User player) {
        try {
            try (Connection connection = _dbAccess.getDataSource().getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement(
                        "select deck_name, format_name, sum(win), sum(lose) from" +
                                " (select winner_deck_name as deck_name, format_name, 1 as win, 0 as lose from game_history where winner=? and (tournament is null or tournament like 'Casual %') and (win_reason <> 'Game cancelled due to error')" +
                                " union all select loser_deck_name as deck_name, format_name, 0 as win, 1 as lose from game_history where loser=? and (tournament is null or tournament like 'Casual %') and (win_reason <> 'Game cancelled due to error')) as u" +
                                " group by deck_name, format_name order by format_name, deck_name")) {
                    statement.setString(1, player.getName());
                    statement.setString(2, player.getName());
                    List<PlayerStatistic> result = new LinkedList<>();
                    try (ResultSet rs = statement.executeQuery()) {
                        while (rs.next())
                            result.add(new PlayerStatistic(rs.getString(1), rs.getString(2), rs.getInt(3), rs.getInt(4)));
                    }
                    return result;
                }
            }
        } catch (SQLException exp) {
            throw new RuntimeException("Unable to get count of games played", exp);
        }
    }

    public final List<PlayerStatistic> getCompetitivePlayerStatistics(User player) {
        try {
            try (Connection connection = _dbAccess.getDataSource().getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement(
                        "select deck_name, format_name, sum(win), sum(lose) from" +
                                " (select winner_deck_name as deck_name, format_name, 1 as win, 0 as lose from game_history where winner=? and (tournament is not null and not tournament like 'Casual %') and (win_reason <> 'Game cancelled due to error')" +
                                " union all select loser_deck_name as deck_name, format_name, 0 as win, 1 as lose from game_history where loser=? and (tournament is not null and not tournament like 'Casual %') and (win_reason <> 'Game cancelled due to error')) as u" +
                                " group by deck_name, format_name order by format_name, deck_name")) {
                    statement.setString(1, player.getName());
                    statement.setString(2, player.getName());
                    List<PlayerStatistic> result = new LinkedList<>();
                    try (ResultSet rs = statement.executeQuery()) {
                        while (rs.next())
                            result.add(new PlayerStatistic(rs.getString(1), rs.getString(2), rs.getInt(3), rs.getInt(4)));
                    }
                    return result;
                }
            }
        } catch (SQLException exp) {
            throw new RuntimeException("Unable to get count of games played", exp);
        }
    }
}