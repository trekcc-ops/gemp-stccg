package com.gempukku.stccg.database;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gempukku.stccg.league.League;

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

    private static final String INSERT_STATEMENT =
            "insert into league_test (properties, start, end) " +
                    "values (?, ?, ?)";
    private static final String SELECT_STATEMENT =
            "select properties from league_test where end>=? order by start desc";
    private final DbAccess _dbAccess;
    private final ObjectMapper _leagueMapper;

    public DbLeagueDAO(DbAccess dbAccess, ObjectMapper leagueMapper) {
        _dbAccess = dbAccess;
        _leagueMapper = leagueMapper;
    }

    public final void addLeague(League league) {
        try {
            String properties = _leagueMapper.writeValueAsString(league);
            String startTimeStamp = convertToTimeStamp(league.getStart());
            String endTimeStamp = convertToTimeStamp(league.getEnd());
            SQLUtils.executeStatementWithParameters(_dbAccess, INSERT_STATEMENT, properties, startTimeStamp,
                    endTimeStamp);
        } catch(SQLException | JsonProcessingException exp) {
            throw new RuntimeException("Unable to add league to database", exp);
        }
    }


    public final List<League> loadActiveLeagues() throws SQLException, JsonProcessingException {
        try (Connection conn = _dbAccess.getDataSource().getConnection()) {
            try (PreparedStatement statement = conn.prepareStatement(SELECT_STATEMENT)) {
                String currentTimestamp = convertToTimeStamp(ZonedDateTime.now());
                statement.setString(1, currentTimestamp);
                try (ResultSet rs = statement.executeQuery()) {
                    List<League> activeLeagues = new ArrayList<>();
                    while (rs.next()) {
                        String properties = rs.getString(1);
                        League league = _leagueMapper.readValue(properties, League.class);
                        activeLeagues.add(league);
                    }
                    return activeLeagues;
                }
            }
        }
    }

    public final void setStatus(League league, int newStatus) {
        try {
            String sqlStatement = "update league set status=? where type=?";
            SQLUtils.executeStatementWithParameters(_dbAccess, sqlStatement,
                    newStatus, league.getType());
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