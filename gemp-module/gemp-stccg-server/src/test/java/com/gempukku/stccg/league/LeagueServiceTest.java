package com.gempukku.stccg.league;

import com.gempukku.stccg.AbstractServerTest;
import com.gempukku.stccg.collection.CollectionsManager;
import com.gempukku.stccg.competitive.PlayerStanding;
import com.gempukku.stccg.db.LeagueDAO;
import com.gempukku.stccg.db.LeagueMatchDAO;
import com.gempukku.stccg.db.LeagueParticipationDAO;
import com.gempukku.stccg.db.vo.League;
import com.gempukku.stccg.db.vo.LeagueMatchResult;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class LeagueServiceTest extends AbstractServerTest {

    @Test
    public void testJoiningLeagueAfterMaxGamesPlayed() throws Exception {

        LeagueDAO leagueDao = Mockito.mock(LeagueDAO.class);

        String sb = "20120502,default,1,1,1" + (",lotr_block,7,2").repeat(1);

        List<League> leagues = new ArrayList<>();
        League league = new League(5000, "League name", "leagueType", NewConstructedLeagueData.class.getName(), sb, 0);
        leagues.add(league);

        LeagueSeriesData leagueSerie = league.getLeagueData(_cardLibrary, _formatLibrary, null).getSeries().getFirst();

        Mockito.when(leagueDao.loadActiveLeagues(Mockito.anyInt())).thenReturn(leagues);

        LeagueMatchDAO leagueMatchDAO = Mockito.mock(LeagueMatchDAO.class);

        Set<LeagueMatchResult> matches = new HashSet<>();

        Mockito.when(leagueMatchDAO.getLeagueMatches(league.getType())).thenReturn(new HashSet<>(matches));

        LeagueParticipationDAO leagueParticipationDAO = Mockito.mock(LeagueParticipationDAO.class);
        CollectionsManager collectionsManager = Mockito.mock(CollectionsManager.class);

        LeagueService leagueService = new LeagueService(leagueDao, leagueMatchDAO, leagueParticipationDAO, collectionsManager, _cardLibrary, _formatLibrary, null);

        assertTrue(leagueService.canPlayRankedGame(league, leagueSerie, "player1"));
        assertTrue(leagueService.canPlayRankedGameAgainst(league, leagueSerie, "player1", "player2"));

        leagueService.reportLeagueGameResult(league, leagueSerie, "player1", "player2");

        assertTrue(leagueService.canPlayRankedGame(league, leagueSerie, "player1"));
        assertFalse(leagueService.canPlayRankedGameAgainst(league, leagueSerie, "player1", "player2"));
        assertTrue(leagueService.canPlayRankedGameAgainst(league, leagueSerie, "player1", "player3"));

        Mockito.verify(leagueMatchDAO).getLeagueMatches(league.getType());

        Mockito.verify(leagueMatchDAO).addPlayedMatch(league.getType(), leagueSerie.getName(), "player1", "player2");
        Mockito.verifyNoMoreInteractions(leagueMatchDAO);

        leagueService.reportLeagueGameResult(league, leagueSerie, "player1", "player3");

        assertFalse(leagueService.canPlayRankedGame(league, leagueSerie, "player1"));
        assertFalse(leagueService.canPlayRankedGameAgainst(league, leagueSerie, "player1", "player2"));
        assertFalse(leagueService.canPlayRankedGameAgainst(league, leagueSerie, "player1", "player3"));

        Mockito.verify(leagueMatchDAO).addPlayedMatch(league.getType(), leagueSerie.getName(), "player1", "player3");
        Mockito.verifyNoMoreInteractions(leagueMatchDAO);
    }

    @Test
    public void testJoiningLeagueAfterMaxGamesPlayedWithPreloadedDb() throws Exception {
        LeagueDAO leagueDao = Mockito.mock(LeagueDAO.class);

        String sb = "20120502,default,1,1,1" + (",lotr_block,7,2").repeat(1);

        List<League> leagues = new ArrayList<>();
        League league = new League(
                5000, "League name", "leagueType", NewConstructedLeagueData.class.getName(), sb, 0
        );
        leagues.add(league);

        LeagueSeriesData leagueSerie = league.getLeagueData(_cardLibrary, _formatLibrary, null).getSeries().getFirst();

        Mockito.when(leagueDao.loadActiveLeagues(Mockito.anyInt())).thenReturn(leagues);

        LeagueMatchDAO leagueMatchDAO = Mockito.mock(LeagueMatchDAO.class);

        Set<LeagueMatchResult> matches = new HashSet<>();
        matches.add(new LeagueMatchResult(leagueSerie.getName(), "player1", "player2"));

        Mockito.when(leagueMatchDAO.getLeagueMatches(league.getType())).thenReturn(new HashSet<>(matches));

        LeagueParticipationDAO leagueParticipationDAO = Mockito.mock(LeagueParticipationDAO.class);
        CollectionsManager collectionsManager = Mockito.mock(CollectionsManager.class);

        LeagueService leagueService = new LeagueService(leagueDao, leagueMatchDAO, leagueParticipationDAO, collectionsManager, _cardLibrary, _formatLibrary, null);

        assertTrue(leagueService.canPlayRankedGame(league, leagueSerie, "player1"));
        assertFalse(leagueService.canPlayRankedGameAgainst(league, leagueSerie, "player1", "player2"));
        assertTrue(leagueService.canPlayRankedGameAgainst(league, leagueSerie, "player1", "player3"));

        leagueService.reportLeagueGameResult(league, leagueSerie, "player1", "player3");

        Mockito.verify(leagueMatchDAO).getLeagueMatches(league.getType());

        Mockito.verify(leagueMatchDAO).addPlayedMatch(league.getType(), leagueSerie.getName(), "player1", "player3");
        Mockito.verifyNoMoreInteractions(leagueMatchDAO);

        assertFalse(leagueService.canPlayRankedGame(league, leagueSerie, "player1"));
        assertFalse(leagueService.canPlayRankedGameAgainst(league, leagueSerie, "player1", "player2"));
        assertFalse(leagueService.canPlayRankedGameAgainst(league, leagueSerie, "player1", "player3"));
    }

    @Test
    public void testStandings() throws Exception {
        LeagueDAO leagueDao = Mockito.mock(LeagueDAO.class);

        String sb = "20120502,default,1,1,1" + (",lotr_block,7,2").repeat(1);

        List<League> leagues = new ArrayList<>();
        League league = new League(5000, "League name", "leagueType", NewConstructedLeagueData.class.getName(), sb, 0);
        leagues.add(league);

        LeagueSeriesData leagueSerie = league.getLeagueData(_cardLibrary, _formatLibrary,null).getSeries().getFirst();

        Mockito.when(leagueDao.loadActiveLeagues(Mockito.anyInt())).thenReturn(leagues);

        LeagueMatchDAO leagueMatchDAO = Mockito.mock(LeagueMatchDAO.class);

        Set<LeagueMatchResult> matches = new HashSet<>();

        Mockito.when(leagueMatchDAO.getLeagueMatches(league.getType())).thenReturn(new HashSet<>(matches));

        LeagueParticipationDAO leagueParticipationDAO = Mockito.mock(LeagueParticipationDAO.class);
        Set<String> players = new HashSet<>();
        players.add("player1");
        players.add("player2");
        players.add("player3");
        Mockito.when(leagueParticipationDAO.getUsersParticipating(league.getType())).thenReturn(players);
        CollectionsManager collectionsManager = Mockito.mock(CollectionsManager.class);

        LeagueService leagueService = new LeagueService(leagueDao, leagueMatchDAO, leagueParticipationDAO, collectionsManager, _cardLibrary, _formatLibrary, null);

        leagueService.reportLeagueGameResult(league, leagueSerie, "player1", "player2");
        leagueService.reportLeagueGameResult(league, leagueSerie, "player1", "player3");
        leagueService.reportLeagueGameResult(league, leagueSerie, "player2", "player3");

        final List<PlayerStanding> leagueSerieStandings = leagueService.getLeagueSerieStandings(league, leagueSerie);
        assertEquals(3, leagueSerieStandings.size());
        assertEquals("player1", leagueSerieStandings.getFirst().getPlayerName());
        assertEquals(4, leagueSerieStandings.getFirst().getPoints());
        assertEquals(2, leagueSerieStandings.get(0).getGamesPlayed());
        assertEquals(1, leagueSerieStandings.get(0).getStanding());
        assertEquals("player2", leagueSerieStandings.get(1).getPlayerName());
        assertEquals(3, leagueSerieStandings.get(1).getPoints());
        assertEquals(2, leagueSerieStandings.get(1).getGamesPlayed());
        assertEquals(2, leagueSerieStandings.get(1).getStanding());
        assertEquals("player3", leagueSerieStandings.get(2).getPlayerName());
        assertEquals(2, leagueSerieStandings.get(2).getPoints());
        assertEquals(2, leagueSerieStandings.get(2).getGamesPlayed());
        assertEquals(3, leagueSerieStandings.get(2).getStanding());

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
