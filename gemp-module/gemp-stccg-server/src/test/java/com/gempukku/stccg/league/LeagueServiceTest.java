package com.gempukku.stccg.league;

import com.gempukku.stccg.AbstractServerTest;
import com.gempukku.stccg.collection.CollectionsManager;
import com.gempukku.stccg.competitive.PlayerStanding;
import com.gempukku.stccg.db.LeagueDAO;
import com.gempukku.stccg.db.LeagueMatchDAO;
import com.gempukku.stccg.db.LeagueParticipationDAO;
import com.gempukku.stccg.competitive.LeagueMatchResult;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("MagicNumber")
public class LeagueServiceTest extends AbstractServerTest {

    private final String leagueData = "20120502,default,1,1,1" + (",test_block,7,2").repeat(1);

    @Test
    public void testJoiningLeagueAfterMaxGamesPlayed() throws Exception {

        LeagueDAO leagueDao = Mockito.mock(LeagueDAO.class);


        List<League> leagues = new ArrayList<>();
        League league = new League(5000, "League name", "leagueType",
                NewConstructedLeagueData.class.getName(), leagueData, 0);
        leagues.add(league);

        LeagueSeriesData seriesData =
                league.getLeagueData(_cardLibrary, _formatLibrary, null).getSeries().getFirst();

        Mockito.when(leagueDao.loadActiveLeagues(Mockito.anyInt())).thenReturn(leagues);

        LeagueMatchDAO leagueMatchDAO = Mockito.mock(LeagueMatchDAO.class);

        Mockito.when(leagueMatchDAO.getLeagueMatches(league.getType())).thenReturn(new HashSet<>());

        LeagueParticipationDAO leagueParticipationDAO = Mockito.mock(LeagueParticipationDAO.class);
        CollectionsManager collectionsManager = Mockito.mock(CollectionsManager.class);

        LeagueService leagueService = new LeagueService(leagueDao, leagueMatchDAO, leagueParticipationDAO,
                collectionsManager, _cardLibrary, _formatLibrary, null);

        assertTrue(leagueService.canPlayRankedGame(league, seriesData, "player1"));
        assertTrue(leagueService.canPlayRankedGameAgainst(league, seriesData, "player1", "player2"));

        leagueService.reportLeagueGameResult(league, seriesData, "player1", "player2");

        assertTrue(leagueService.canPlayRankedGame(league, seriesData, "player1"));
        assertFalse(leagueService.canPlayRankedGameAgainst(league, seriesData, "player1", "player2"));
        assertTrue(leagueService.canPlayRankedGameAgainst(league, seriesData, "player1", "player3"));

        Mockito.verify(leagueMatchDAO).getLeagueMatches(league.getType());
        Mockito.verify(leagueMatchDAO).addPlayedMatch(
                league.getType(), seriesData.getName(), "player1", "player2");
        Mockito.verifyNoMoreInteractions(leagueMatchDAO);

        leagueService.reportLeagueGameResult(league, seriesData, "player1", "player3");

        assertFalse(leagueService.canPlayRankedGame(league, seriesData, "player1"));
        assertFalse(leagueService.canPlayRankedGameAgainst(league, seriesData, "player1", "player2"));
        assertFalse(leagueService.canPlayRankedGameAgainst(league, seriesData, "player1", "player3"));

        Mockito.verify(leagueMatchDAO).addPlayedMatch(
                league.getType(), seriesData.getName(), "player1", "player3");
        Mockito.verifyNoMoreInteractions(leagueMatchDAO);
    }

    @Test
    public void testJoiningLeagueAfterMaxGamesPlayedWithPreloadedDb() throws Exception {
        LeagueDAO leagueDao = Mockito.mock(LeagueDAO.class);

        List<League> leagues = new ArrayList<>();
        League league = new League(
                5000, "League name", "leagueType", NewConstructedLeagueData.class.getName(), leagueData, 0
        );
        leagues.add(league);

        LeagueSeriesData seriesData = league.getLeagueData(_cardLibrary, _formatLibrary, null).getSeries().getFirst();

        Mockito.when(leagueDao.loadActiveLeagues(Mockito.anyInt())).thenReturn(leagues);

        LeagueMatchDAO leagueMatchDAO = Mockito.mock(LeagueMatchDAO.class);

        Set<LeagueMatchResult> matches = new HashSet<>();
        matches.add(new LeagueMatchResult(seriesData.getName(), "player1", "player2"));

        Mockito.when(leagueMatchDAO.getLeagueMatches(league.getType())).thenReturn(new HashSet<>(matches));

        LeagueParticipationDAO leagueParticipationDAO = Mockito.mock(LeagueParticipationDAO.class);
        CollectionsManager collectionsManager = Mockito.mock(CollectionsManager.class);

        LeagueService leagueService = new LeagueService(leagueDao, leagueMatchDAO, leagueParticipationDAO, collectionsManager, _cardLibrary, _formatLibrary, null);

        assertTrue(leagueService.canPlayRankedGame(league, seriesData, "player1"));
        assertFalse(leagueService.canPlayRankedGameAgainst(league, seriesData, "player1", "player2"));
        assertTrue(leagueService.canPlayRankedGameAgainst(league, seriesData, "player1", "player3"));

        leagueService.reportLeagueGameResult(league, seriesData, "player1", "player3");

        Mockito.verify(leagueMatchDAO).getLeagueMatches(league.getType());

        Mockito.verify(leagueMatchDAO).addPlayedMatch(league.getType(), seriesData.getName(), "player1", "player3");
        Mockito.verifyNoMoreInteractions(leagueMatchDAO);

        assertFalse(leagueService.canPlayRankedGame(league, seriesData, "player1"));
        assertFalse(leagueService.canPlayRankedGameAgainst(league, seriesData, "player1", "player2"));
        assertFalse(leagueService.canPlayRankedGameAgainst(league, seriesData, "player1", "player3"));
    }

    @Test
    public void testStandings() throws Exception {
        LeagueDAO leagueDao = Mockito.mock(LeagueDAO.class);

        List<League> leagues = new ArrayList<>();
        League league = new League(5000, "League name", "leagueType", NewConstructedLeagueData.class.getName(), leagueData, 0);
        leagues.add(league);

        LeagueSeriesData seriesData = league.getLeagueData(_cardLibrary, _formatLibrary,null).getSeries().getFirst();

        Mockito.when(leagueDao.loadActiveLeagues(Mockito.anyInt())).thenReturn(leagues);

        LeagueMatchDAO leagueMatchDAO = Mockito.mock(LeagueMatchDAO.class);

        Mockito.when(leagueMatchDAO.getLeagueMatches(league.getType())).thenReturn(new HashSet<>());

        LeagueParticipationDAO leagueParticipationDAO = Mockito.mock(LeagueParticipationDAO.class);
        Set<String> players = new HashSet<>();
        players.add("player1");
        players.add("player2");
        players.add("player3");
        Mockito.when(leagueParticipationDAO.getUsersParticipating(league.getType())).thenReturn(players);
        CollectionsManager collectionsManager = Mockito.mock(CollectionsManager.class);

        LeagueService leagueService = new LeagueService(leagueDao, leagueMatchDAO, leagueParticipationDAO, collectionsManager, _cardLibrary, _formatLibrary, null);

        leagueService.reportLeagueGameResult(league, seriesData, "player1", "player2");
        leagueService.reportLeagueGameResult(league, seriesData, "player1", "player3");
        leagueService.reportLeagueGameResult(league, seriesData, "player2", "player3");

        final List<PlayerStanding> seriesStandings = leagueService.getLeagueSeriesStandings(league, seriesData);
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