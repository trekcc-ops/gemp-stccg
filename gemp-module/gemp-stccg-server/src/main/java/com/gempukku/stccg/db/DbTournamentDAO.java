package com.gempukku.stccg.db;

import com.gempukku.stccg.collection.CollectionType;
import com.gempukku.stccg.tournament.Tournament;
import com.gempukku.stccg.tournament.TournamentDAO;
import com.gempukku.stccg.tournament.TournamentInfo;
import com.gempukku.stccg.tournament.TournamentQueueInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class DbTournamentDAO implements TournamentDAO {
    private static final Logger LOGGER = LogManager.getLogger(DbTournamentDAO.class);
    private final DbAccess _dbAccess;
    private final String _tournamentFields =
            "tournament_id, draft_type, name, format, collection, stage, pairing, round, prizes";

    public DbTournamentDAO(DbAccess dbAccess) {
        _dbAccess = dbAccess;
    }

    @Override
    public void addTournament(String tournamentId, String draftType, String tournamentName, String format,
                              CollectionType collectionType, Tournament.Stage stage, String pairingMechanism,
                              String prizesScheme, Date start) {
        try {
            String sqlMessage =
                    "insert into tournament (" + _tournamentFields + ") values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            String collectionString = collectionType.getCode() + ":" + collectionType.getFullName();
            SQLUtils.executeStatementWithParameters(_dbAccess, sqlMessage,
                    tournamentId, draftType, tournamentName, format, collectionString,
                    stage.name(), pairingMechanism, start.getTime(), 0, prizesScheme);
        } catch (SQLException exp) {
            throw new RuntimeException(exp);
        }
    }

    @Override
    public TournamentInfo getTournamentById(String tournamentId) {
        try {
            try (Connection connection = _dbAccess.getDataSource().getConnection()) {
                String sqlMessage = "select " + _tournamentFields + " from tournament where tournament_id=?";
                try (PreparedStatement statement = connection.prepareStatement(sqlMessage)) {
                    statement.setString(1, tournamentId);
                    ResultSet rs = statement.executeQuery();
                    return (rs.next()) ? createTournamentInfoFromResultSet(rs) : null;
                }
            }
        } catch (SQLException exp) {
            throw new RuntimeException(exp);
        }
    }

    @Override
    public List<TournamentInfo> getUnfinishedTournaments() {
        LOGGER.debug("Called getUnfinishedTournaments function");
        LOGGER.debug("getUnfinishedTournaments function - attempting connection to " + _dbAccess.getDataSource());
        try {
            try (Connection connection = _dbAccess.getDataSource().getConnection()) {
                String sqlMessage = "select " + _tournamentFields + " from tournament where stage <> '" +
                        Tournament.Stage.FINISHED.name() + "'";
                try (PreparedStatement statement = connection.prepareStatement(sqlMessage)) {
                    try (ResultSet rs = statement.executeQuery()) {
                        List<TournamentInfo> result = new ArrayList<>();
                        while (rs.next())
                            result.add(createTournamentInfoFromResultSet(rs));
                        return result;
                    }
                }
            }
        } catch (SQLException exp) {
            throw new RuntimeException(exp);
        }
    }

    private TournamentInfo createTournamentInfoFromResultSet(ResultSet rs) throws SQLException {
        // Assumes rs is a ResultSet with _tournamentFields in that order
        String[] collectionTypeStr = rs.getString(5).split(":", 2);
        return new TournamentInfo(
                // rs.getString(2) is draftType, which is not currently being used
                rs.getString(1), rs.getString(3),
                rs.getString(4),
                new CollectionType(collectionTypeStr[0], collectionTypeStr[1]),
                Tournament.Stage.valueOf(rs.getString(6)),
                rs.getString(7), rs.getString(9), rs.getInt(8));
    }

    @Override
    public List<TournamentInfo> getFinishedTournamentsSince(long time) {
        try {
            try (Connection connection = _dbAccess.getDataSource().getConnection()) {
                String sqlMessage = "select " + _tournamentFields + " from tournament where stage = '" +
                        Tournament.Stage.FINISHED.name() + "' and start>?";
                try (PreparedStatement statement = connection.prepareStatement(sqlMessage)) {
                    statement.setLong(1, time);
                    try (ResultSet rs = statement.executeQuery()) {
                        List<TournamentInfo> result = new ArrayList<>();
                        while (rs.next())
                            result.add(createTournamentInfoFromResultSet(rs));
                        return result;
                    }
                }
            }
        } catch (SQLException exp) {
            throw new RuntimeException(exp);
        }
    }

    @Override
    public void updateTournamentStage(String tournamentId, Tournament.Stage stage) {
        try {
            String sqlStatement = "update tournament set stage=? where tournament_id=?";
            SQLUtils.executeUpdateStatementWithParameters(_dbAccess, sqlStatement,
                    stage.name(), tournamentId);
        } catch (SQLException exp) {
            throw new RuntimeException(exp);
        }
    }

    @Override
    public void updateTournamentRound(String tournamentId, int round) {
        try {
            String sqlStatement = "update tournament set round=? where tournament_id=?";
            SQLUtils.executeUpdateStatementWithParameters(_dbAccess, sqlStatement,
                    round, tournamentId);
        } catch (SQLException exp) {
            throw new RuntimeException(exp);
        }
    }

    @Override
    public List<TournamentQueueInfo> getFutureScheduledTournamentQueues(long tillDate) {
        try {
            try (Connection connection = _dbAccess.getDataSource().getConnection()) {
                String sqlMessage =
                        "select tournament_id, name, format, start, cost, playoff, prizes, minimum_players " +
                                "from scheduled_tournament where started = 0 and start<=?";
                try (PreparedStatement statement = connection.prepareStatement(sqlMessage)) {
                    statement.setLong(1, tillDate);
                    try (ResultSet rs = statement.executeQuery()) {
                        List<TournamentQueueInfo> result = new ArrayList<>();
                        while (rs.next()) {
                            result.add(new TournamentQueueInfo(rs.getString(1), rs.getString(2),
                                    rs.getString(3), rs.getLong(4), rs.getInt(5),
                                    rs.getString(6), rs.getString(7), rs.getInt(8))
                            );
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
    public void updateScheduledTournamentStarted(String scheduledTournamentId) {
        try {
            String sqlStatement = "update scheduled_tournament set started=1 where tournament_id=?";
            SQLUtils.executeUpdateStatementWithParameters(_dbAccess, sqlStatement,
                    scheduledTournamentId);
        } catch (SQLException exp) {
            throw new RuntimeException(exp);
        }
    }
}