package com.gempukku.stccg.league;

import com.gempukku.stccg.AbstractServerTest;
import com.gempukku.stccg.collection.CollectionType;
import com.gempukku.stccg.collection.CollectionsManager;
import com.gempukku.stccg.competitive.LeagueMatchResult;
import com.gempukku.stccg.competitive.PlayerStanding;
import com.gempukku.stccg.database.LeagueDAO;
import com.gempukku.stccg.database.LeagueMatchDAO;
import com.gempukku.stccg.database.LeagueParticipationDAO;
import com.gempukku.stccg.formats.GameFormat;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings({"MagicNumber", "LongLine"})
public class LeagueServiceTest extends AbstractServerTest {

    private final Set<String> _playersParticipating = Set.of("player1", "player2", "player3");
    private final LeagueDAO leagueDao = Mockito.mock(LeagueDAO.class);
    private final League league = createLeague();
    private final List<League> leagues = List.of(league);
    private final LeagueSeries series = league.getAllSeries().getFirst();
    LeagueMatchDAO leagueMatchDAO = Mockito.mock(LeagueMatchDAO.class);
    LeagueParticipationDAO leagueParticipationDAO = Mockito.mock(LeagueParticipationDAO.class);
    CollectionsManager collectionsManager = Mockito.mock(CollectionsManager.class);
    private LeagueService leagueService;
    private Set<LeagueMatchResult> matches;

    private League createLeague() {
        ZonedDateTime startTime = ZonedDateTime.parse("2012-05-02T00:00:00Z");
        List<LeagueSeries> allSeries = new ArrayList<>();
        GameFormat format = _formatLibrary.get("debug1e");
        allSeries.add(new LeagueSeries(7, 2, startTime, format, "Week 1"));
        return new ConstructedLeague(5000, "League name", CollectionType.ALL_CARDS,
                allSeries, _cardLibrary);
    }

    private void setUpMockitoCalls() throws Exception {
        Mockito.when(leagueDao.loadActiveLeagues()).thenReturn(leagues);
        Mockito.when(leagueMatchDAO.getLeagueMatches(league.getLeagueId())).thenReturn(matches);
        Mockito.when(leagueParticipationDAO.getUsersParticipating(league.getLeagueId())).thenReturn(_playersParticipating);
        leagueService = new LeagueService(leagueDao, leagueMatchDAO, leagueParticipationDAO,
                collectionsManager);
    }


    @Test
    public void testJoiningLeagueAfterMaxGamesPlayed() throws Exception {

        matches = new HashSet<>();
        setUpMockitoCalls();

        assertTrue(leagueService.canPlayRankedGame(league, series, "player1"));
        assertTrue(leagueService.canPlayRankedGameAgainst(league, series, "player1", "player2"));

        leagueService.reportLeagueGameResult(league, series, "player1", "player2");

        assertTrue(leagueService.canPlayRankedGame(league, series, "player1"));
        assertFalse(leagueService.canPlayRankedGameAgainst(league, series, "player1", "player2"));
        assertTrue(leagueService.canPlayRankedGameAgainst(league, series, "player1", "player3"));

        Mockito.verify(leagueMatchDAO).getLeagueMatches(league.getLeagueId());
        Mockito.verify(leagueMatchDAO).addPlayedMatch(
                league.getLeagueId(), series.getName(), "player1", "player2");
        Mockito.verifyNoMoreInteractions(leagueMatchDAO);

        leagueService.reportLeagueGameResult(league, series, "player1", "player3");

        assertFalse(leagueService.canPlayRankedGame(league, series, "player1"));
        assertFalse(leagueService.canPlayRankedGameAgainst(league, series, "player1", "player2"));
        assertFalse(leagueService.canPlayRankedGameAgainst(league, series, "player1", "player3"));

        Mockito.verify(leagueMatchDAO).addPlayedMatch(
                league.getLeagueId(), series.getName(), "player1", "player3");
        Mockito.verifyNoMoreInteractions(leagueMatchDAO);
    }

    @Test
    public void testJoiningLeagueAfterMaxGamesPlayedWithPreloadedDb() throws Exception {

        matches = Set.of(new LeagueMatchResult(series.getName(), "player1", "player2"));
        setUpMockitoCalls();

        assertTrue(leagueService.canPlayRankedGame(league, series, "player1"));
        assertFalse(leagueService.canPlayRankedGameAgainst(league, series, "player1", "player2"));
        assertTrue(leagueService.canPlayRankedGameAgainst(league, series, "player1", "player3"));

        leagueService.reportLeagueGameResult(league, series, "player1", "player3");

        Mockito.verify(leagueMatchDAO).getLeagueMatches(league.getLeagueId());

        Mockito.verify(leagueMatchDAO).addPlayedMatch(league.getLeagueId(), series.getName(), "player1", "player3");
        Mockito.verifyNoMoreInteractions(leagueMatchDAO);

        assertFalse(leagueService.canPlayRankedGame(league, series, "player1"));
        assertFalse(leagueService.canPlayRankedGameAgainst(league, series, "player1", "player2"));
        assertFalse(leagueService.canPlayRankedGameAgainst(league, series, "player1", "player3"));
    }

    @Test
    public void testStandings() throws Exception {

        matches = new HashSet<>();
        setUpMockitoCalls();

        leagueService.reportLeagueGameResult(league, series, "player1", "player2");
        leagueService.reportLeagueGameResult(league, series, "player1", "player3");
        leagueService.reportLeagueGameResult(league, series, "player2", "player3");

        final List<PlayerStanding> seriesStandings = leagueService.getLeagueSeriesStandings(league, series);
        assertEquals(3, seriesStandings.size());
        assertEquals("player1", seriesStandings.getFirst().getPlayerName());
        assertEquals(4, seriesStandings.getFirst().getPoints());
        assertEquals(2, seriesStandings.get(0).getGamesPlayed());
        assertEquals(1, seriesStandings.get(0).getStanding());
        assertEquals("player2", seriesStandings.get(1).getPlayerName());
        assertEquals(3, seriesStandings.get(1).getPoints());
        assertEquals(2, seriesStandings.get(1).getGamesPlayed());
        assertEquals(2, seriesStandings.get(1).getStanding());
        assertEquals("player3", seriesStandings.get(2).getPlayerName());
        assertEquals(2, seriesStandings.get(2).getPoints());
        assertEquals(2, seriesStandings.get(2).getGamesPlayed());
        assertEquals(3, seriesStandings.get(2).getStanding());

        final List<PlayerStanding> leagueStandings = leagueService.getLeagueStandings(league);
        assertEquals(3, leagueStandings.size());
        assertEquals("player1", leagueStandings.getFirst().getPlayerName());
        assertEquals(4, leagueStandings.getFirst().getPoints());
        assertEquals(2, leagueStandings.get(0).getGamesPlayed());
        assertEquals(1, leagueStandings.get(0).getStanding());
        assertEquals("player2", leagueStandings.get(1).getPlayerName());
        assertEquals(3, leagueStandings.get(1).getPoints());
        assertEquals(2, leagueStandings.get(1).getGamesPlayed());
        assertEquals(2, leagueStandings.get(1).getStanding());
        assertEquals("player3", leagueStandings.get(2).getPlayerName());
        assertEquals(2, leagueStandings.get(2).getPoints());
        assertEquals(2, leagueStandings.get(2).getGamesPlayed());
        assertEquals(3, leagueStandings.get(2).getStanding());
    }
}