package com.gempukku.stccg.database;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gempukku.stccg.league.League;
import com.gempukku.stccg.league.LeagueMapper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class DbLeagueDAO implements LeagueDAO {

    private static final String INSERT_STATEMENT = "INSERT INTO league (properties, start, end) VALUES (?, ?, ?)";
    private static final String SELECT_STATEMENT =
            "SELECT league_id, properties FROM league WHERE end>=? ORDER BY start DESC";
    private static final String UPDATE_STATUS_STATEMENT = "UPDATE league SET status=? WHERE league_id=?";
    private final DbAccess _dbAccess;
    private final LeagueMapper _leagueMapper;

    public DbLeagueDAO(DbAccess dbAccess, LeagueMapper leagueMapper) {
        _dbAccess = dbAccess;
        _leagueMapper = leagueMapper;
    }

    public final void addLeague(League league) {
        try {
            String properties = _leagueMapper.writeLeagueAsJsonString(league);
            String startTimeStamp = convertToTimeStamp(league.getStart());
            String endTimeStamp = convertToTimeStamp(league.getEnd());
            SQLUtils.executeStatementWithParameters(_dbAccess, INSERT_STATEMENT, properties, startTimeStamp,
                    endTimeStamp);
        } catch(SQLException | JsonProcessingException exp) {
            throw new RuntimeException("Unable to add league to database", exp);
        }
    }

    public final List<League> loadActiveLeagues() throws SQLException {
        try (Connection conn = _dbAccess.getDataSource().getConnection()) {
            try (PreparedStatement statement = conn.prepareStatement(SELECT_STATEMENT)) {
                String currentTimestamp = convertToTimeStamp(ZonedDateTime.now());
                statement.setString(1, currentTimestamp);
                try (ResultSet rs = statement.executeQuery()) {
                    List<League> activeLeagues = new ArrayList<>();
                    while (rs.next()) {
                        int leagueId = rs.getInt(1);
                        String properties = rs.getString(2);
                        try {
                            JsonNode propertiesJson = _leagueMapper.readTree(properties);
                            ((ObjectNode) propertiesJson).put("leagueId", leagueId);
                            League league = _leagueMapper.treeToValue(propertiesJson, League.class);
                            activeLeagues.add(league);
                        } catch(Exception exp) {
                            throw new SQLException("Unable to deserialize league with id " + leagueId, exp);
                        }
                    }
                    return activeLeagues;
                }
            }
        }
    }

    public final void setStatus(League league) {
        try {
            int status = league.getStatus();
            int leagueId = league.getLeagueId();
            SQLUtils.executeStatementWithParameters(_dbAccess, UPDATE_STATUS_STATEMENT, status, leagueId);
        } catch (SQLException exp) {
            throw new RuntimeException("Unable to update league status", exp);
        }
    }

    private String convertToTimeStamp(ZonedDateTime dateTime) {
        DateTimeFormatter timeStampFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime utcLocalDateTime = LocalDateTime.ofInstant(dateTime.toInstant(), ZoneOffset.UTC);
        return utcLocalDateTime.format(timeStampFormatter);
    }


}