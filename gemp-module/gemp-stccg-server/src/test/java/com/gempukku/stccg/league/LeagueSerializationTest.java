package com.gempukku.stccg.league;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gempukku.stccg.AbstractServerTest;
import com.gempukku.stccg.collection.CollectionType;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LeagueSerializationTest extends AbstractServerTest {

    private final ZonedDateTime LEAGUE_START_TIME = ZonedDateTime.of(
            LocalDateTime.of(2025, 12, 15, 0, 0, 0),
            ZoneId.of("America/Chicago"));
    private final static int LEAGUE_ID = -999; // assigned by default when not pulled from database
    private final static int LEAGUE_STATUS = 1;
    private final static int COST = 100;
    private final static String LEAGUE_NAME = "MyLeague";
    private final static int SERIES_COUNT = 3;
    private final static int SERIES_DURATION = 7;
    private final static int MAX_MATCHES = 2;
    private final static String GAME_FORMAT_CODE = "debug1e";

    @Test
    void serializeConstructedLeagueTest() throws JsonProcessingException {
        CollectionType collectionType = CollectionType.ALL_CARDS;
        List<Integer> durations = Collections.nCopies(SERIES_COUNT, SERIES_DURATION);
        List<Integer> maxMatches = Collections.nCopies(SERIES_COUNT, MAX_MATCHES);
        List<String> formats = Collections.nCopies(SERIES_COUNT, GAME_FORMAT_CODE);
        League league = new ConstructedLeague(COST, LEAGUE_NAME, collectionType, _formatLibrary, _cardLibrary, durations,
                maxMatches, formats, LEAGUE_START_TIME);
        league.setStatus(LEAGUE_STATUS);
        verifyLeagueProperties(league);

        ObjectMapper mapper = new LeagueMapper(_cardLibrary, _formatLibrary, _draftFormatLibrary);
        String jsonString = mapper.writeValueAsString(league);
        League leagueCopy = mapper.readValue(jsonString, League.class);
        verifyLeagueProperties(leagueCopy);
    }

    private void verifyLeagueProperties(League league) {
        assertEquals(SERIES_COUNT, league.getAllSeries().size());
        assertEquals(COST, league.getCost());
        assertEquals(LEAGUE_ID, league.getLeagueId());
        assertEquals(LEAGUE_STATUS, league.getStatus());
        assertEquals(league.getEnd(), league.getStart().plusDays(SERIES_DURATION * SERIES_COUNT));
        for (LeagueSeries series : league) {
            assertEquals(series.getEnd(), series.getStart().plusDays(SERIES_DURATION));
            assertEquals(MAX_MATCHES, series.getMaxMatches());
        }
    }
}