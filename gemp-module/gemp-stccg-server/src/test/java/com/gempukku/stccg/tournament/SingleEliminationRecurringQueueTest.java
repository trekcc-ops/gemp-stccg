package com.gempukku.stccg.tournament;

import com.gempukku.stccg.AbstractServerTest;
import com.gempukku.stccg.collection.CollectionsManager;
import com.gempukku.stccg.collection.CollectionType;
import com.gempukku.stccg.db.User;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

public class SingleEliminationRecurringQueueTest extends AbstractServerTest {
    @Test
    public void joiningQueue() throws SQLException, IOException {
        TournamentService tournamentService = Mockito.mock(TournamentService.class);

        ImmediateRecurringQueue queue = new ImmediateRecurringQueue(10, "format", CollectionType.MY_CARDS,
                "id-", "name-", 2, false, tournamentService,
                new NoPrizes(), new SingleEliminationPairing("singleElimination"));

        User player = new User(1, "p1", "pass", "u",
                null, null, null, null);

        CollectionsManager collectionsManager = Mockito.mock(CollectionsManager.class);

        Mockito.when(collectionsManager.removeCurrencyFromPlayerCollection(
                Mockito.anyString(), Mockito.eq(player), Mockito.eq(CollectionType.MY_CARDS), Mockito.eq(10)))
                .thenReturn(true);

        queue.joinPlayer(collectionsManager, player, null);

        Mockito.verify(collectionsManager).removeCurrencyFromPlayerCollection(
                Mockito.anyString(), Mockito.eq(player), Mockito.eq(CollectionType.MY_CARDS), Mockito.eq(10));
        Mockito.verifyNoMoreInteractions(collectionsManager, tournamentService);

        assertEquals(1, queue.getPlayerCount());
        assertTrue(queue.isPlayerSignedUp("p1"));
    }

    @Test
    public void leavingQueue() throws SQLException, IOException {
        TournamentService tournamentService = Mockito.mock(TournamentService.class);

        ImmediateRecurringQueue queue = new ImmediateRecurringQueue(10, "format", CollectionType.MY_CARDS,
                "id-", "name-", 2, false, tournamentService, new NoPrizes(), new SingleEliminationPairing("singleElimination"));

        User player = new User(1, "p1", "pass", "u", null, null, null, null);

        CollectionsManager collectionsManager = Mockito.mock(CollectionsManager.class);
        Mockito.when(collectionsManager.removeCurrencyFromPlayerCollection(Mockito.anyString(), Mockito.eq(player), Mockito.eq(CollectionType.MY_CARDS), Mockito.eq(10)))
                .thenReturn(true);

        queue.joinPlayer(collectionsManager, player, null);

        Mockito.verify(collectionsManager).removeCurrencyFromPlayerCollection(Mockito.anyString(), Mockito.eq(player), Mockito.eq(CollectionType.MY_CARDS), Mockito.eq(10));

        queue.leavePlayer(collectionsManager, player);
        Mockito.verify(collectionsManager).addCurrencyToPlayerCollection(Mockito.anyBoolean(), Mockito.anyString(), Mockito.eq(player), Mockito.eq(CollectionType.MY_CARDS), Mockito.eq(10));
        Mockito.verifyNoMoreInteractions(collectionsManager, tournamentService);

        assertEquals(0, queue.getPlayerCount());
        assertFalse(queue.isPlayerSignedUp("p1"));
    }
    
    @Test
    public void cancellingQueue() throws SQLException, IOException {
        TournamentService tournamentService = Mockito.mock(TournamentService.class);

        ImmediateRecurringQueue queue = new ImmediateRecurringQueue(10, "format", CollectionType.MY_CARDS,
                "id-", "name-", 2, false, tournamentService, new NoPrizes(), new SingleEliminationPairing("singleElimination"));

        User player = new User(1, "p1", "pass", "u", null, null, null, null);

        CollectionsManager collectionsManager = Mockito.mock(CollectionsManager.class);
        Mockito.when(collectionsManager.removeCurrencyFromPlayerCollection(Mockito.anyString(), Mockito.eq(player), Mockito.eq(CollectionType.MY_CARDS), Mockito.eq(10)))
                .thenReturn(true);

        queue.joinPlayer(collectionsManager, player, null);

        Mockito.verify(collectionsManager).removeCurrencyFromPlayerCollection(Mockito.anyString(), Mockito.eq(player), Mockito.eq(CollectionType.MY_CARDS), Mockito.eq(10));

        queue.leaveAllPlayers(collectionsManager);
        Mockito.verify(collectionsManager).addCurrencyToPlayerCollection(Mockito.anyBoolean(), Mockito.anyString(), Mockito.eq("p1"), Mockito.eq(CollectionType.MY_CARDS), Mockito.eq(10));
        Mockito.verifyNoMoreInteractions(collectionsManager, tournamentService);

        assertEquals(0, queue.getPlayerCount());
        assertFalse(queue.isPlayerSignedUp("p1"));
    }

    @Test
    public void fillingQueue() throws SQLException, IOException {
        Tournament tournament = Mockito.mock(Tournament.class);
        TournamentService tournamentService = Mockito.mock(TournamentService.class);
        Mockito.when(tournamentService.addTournament(Mockito.anyString(), Mockito.eq(null), Mockito.anyString(), Mockito.eq("format"),
                Mockito.eq(CollectionType.MY_CARDS), Mockito.eq(Tournament.Stage.PLAYING_GAMES), Mockito.eq("singleElimination"), Mockito.nullable(String.class), Mockito.any()))
                .thenReturn(tournament);

        ImmediateRecurringQueue queue = new ImmediateRecurringQueue(10, "format", CollectionType.MY_CARDS,
                "id-", "name-", 2, false, tournamentService, new NoPrizes(), new SingleEliminationPairing("singleElimination"));


        User player1 = new User(1, "p1", "pass", "u", null, null, null, null);
        User player2 = new User(2, "p2", "pass", "u", null, null, null, null);

        CollectionsManager collectionsManager = Mockito.mock(CollectionsManager.class);
        Mockito.when(collectionsManager.removeCurrencyFromPlayerCollection(Mockito.anyString(), Mockito.any(), Mockito.eq(CollectionType.MY_CARDS), Mockito.eq(10)))
                .thenReturn(true);

        queue.joinPlayer(collectionsManager, player1, null);

        TournamentQueueCallback queueCallback = Mockito.mock(TournamentQueueCallback.class);
        assertFalse(queue.process(queueCallback, collectionsManager));

        Mockito.verifyNoMoreInteractions(queueCallback);

        queue.joinPlayer(collectionsManager, player2, null);

        assertEquals(2, queue.getPlayerCount());

        assertFalse(queue.process(queueCallback, collectionsManager));

        assertEquals(0, queue.getPlayerCount());
        assertFalse(queue.isPlayerSignedUp("p1"));
        assertFalse(queue.isPlayerSignedUp("p2"));

        Mockito.verify(tournamentService).addTournament(Mockito.anyString(), Mockito.eq(null), Mockito.anyString(), Mockito.eq("format"),
                Mockito.eq(CollectionType.MY_CARDS), Mockito.eq(Tournament.Stage.PLAYING_GAMES), Mockito.eq("singleElimination"), Mockito.nullable(String.class), Mockito.any());
        
        Mockito.verify(tournamentService).addPlayer(Mockito.anyString(), Mockito.eq("p1"), Mockito.eq(null));
        Mockito.verify(tournamentService).addPlayer(Mockito.anyString(), Mockito.eq("p2"), Mockito.eq(null));

        Mockito.verify(queueCallback).createTournament(tournament);
        Mockito.verifyNoMoreInteractions(tournamentService, queueCallback);
    }

    @Test
    public void overflowingQueue() throws SQLException, IOException {
        Tournament tournament = Mockito.mock(Tournament.class);

        TournamentService tournamentService = Mockito.mock(TournamentService.class);
        Mockito.when(tournamentService.addTournament(Mockito.anyString(), Mockito.eq(null), Mockito.anyString(), Mockito.eq("format"),
                Mockito.eq(CollectionType.MY_CARDS), Mockito.eq(Tournament.Stage.PLAYING_GAMES), Mockito.eq("singleElimination"), Mockito.nullable(String.class), Mockito.any()))
                .thenReturn(tournament);

        ImmediateRecurringQueue queue = new ImmediateRecurringQueue(10, "format", CollectionType.MY_CARDS,
                "id-", "name-", 2, false, tournamentService, new NoPrizes(), new SingleEliminationPairing("singleElimination"));

        User player1 = new User(1, "p1", "pass", "u", null, null, null, null);
        User player2 = new User(2, "p2", "pass", "u", null, null, null, null);
        User player3 = new User(3, "p3", "pass", "u", null, null, null, null);

        CollectionsManager collectionsManager = Mockito.mock(CollectionsManager.class);
        Mockito.when(collectionsManager.removeCurrencyFromPlayerCollection(Mockito.anyString(), Mockito.any(), Mockito.eq(CollectionType.MY_CARDS), Mockito.eq(10)))
                .thenReturn(true);

        queue.joinPlayer(collectionsManager, player1, null);

        TournamentQueueCallback queueCallback = Mockito.mock(TournamentQueueCallback.class);
        assertFalse(queue.process(queueCallback, collectionsManager));

        Mockito.verifyNoMoreInteractions(queueCallback);

        queue.joinPlayer(collectionsManager, player2, null);
        queue.joinPlayer(collectionsManager, player3, null);

        assertEquals(3, queue.getPlayerCount());

        assertFalse(queue.process(queueCallback, collectionsManager));

        assertEquals(1, queue.getPlayerCount());
        assertFalse(queue.isPlayerSignedUp("p1"));
        assertFalse(queue.isPlayerSignedUp("p2"));
        assertTrue(queue.isPlayerSignedUp("p3"));

        Mockito.verify(tournamentService).addTournament(Mockito.anyString(), Mockito.eq(null), Mockito.anyString(), Mockito.eq("format"),
                Mockito.eq(CollectionType.MY_CARDS), Mockito.eq(Tournament.Stage.PLAYING_GAMES), Mockito.eq("singleElimination"), Mockito.nullable(String.class), Mockito.any());

        Mockito.verify(tournamentService).addPlayer(Mockito.anyString(), Mockito.eq("p1"), Mockito.eq(null));
        Mockito.verify(tournamentService).addPlayer(Mockito.anyString(), Mockito.eq("p2"), Mockito.eq(null));

        Mockito.verify(queueCallback).createTournament(tournament);
        Mockito.verifyNoMoreInteractions(tournamentService, queueCallback);
    }
}