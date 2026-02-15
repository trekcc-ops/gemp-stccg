package com.gempukku.stccg.tournament;

import com.gempukku.stccg.AbstractServerTest;
import com.gempukku.stccg.collection.CollectionType;
import com.gempukku.stccg.collection.CollectionsManager;
import com.gempukku.stccg.database.User;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings({"LongLine", "StaticMethodReferencedViaSubclass"})
public class SingleEliminationRecurringQueueTest extends AbstractServerTest {

    private ImmediateRecurringQueue createQueue(TournamentService tournamentService) {
        return new ImmediateRecurringQueue(10, _formatLibrary.get("format"), CollectionType.MY_CARDS,
                "id-", "name-", 2, false, tournamentService,
                new NoPrizes(), new SingleEliminationPairing("singleElimination"));
    }

    @Test
    public void joiningQueue() throws SQLException, IOException {
        TournamentService tournamentService = Mockito.mock(TournamentService.class);
        ImmediateRecurringQueue queue = createQueue(tournamentService);

        User player = new User(1, "p1", "pass", "u",
                null, null, null, null);

        CollectionsManager collectionsManager = Mockito.mock(CollectionsManager.class);

        Mockito.when(collectionsManager.removeCurrencyFromPlayerCollection(
                Mockito.anyString(), Mockito.eq(player), Mockito.eq(10)))
                .thenReturn(true);

        queue.joinPlayer(collectionsManager, player, null);

        Mockito.verify(collectionsManager).removeCurrencyFromPlayerCollection(
                Mockito.anyString(), Mockito.eq(player), Mockito.eq(10));
        Mockito.verifyNoMoreInteractions(collectionsManager, tournamentService);

        assertEquals(1, queue.getPlayerCount());
        assertTrue(queue.isPlayerSignedUp("p1"));
    }

    @Test
    public void leavingQueue() throws SQLException, IOException {
        TournamentService tournamentService = Mockito.mock(TournamentService.class);
        ImmediateRecurringQueue queue = createQueue(tournamentService);

        User player = new User(1, "p1", "pass", "u", null, null, null, null);

        CollectionsManager collectionsManager = Mockito.mock(CollectionsManager.class);
        Mockito.when(collectionsManager.removeCurrencyFromPlayerCollection(Mockito.anyString(), Mockito.eq(player), Mockito.eq(10)))
                .thenReturn(true);

        queue.joinPlayer(collectionsManager, player, null);

        Mockito.verify(collectionsManager).removeCurrencyFromPlayerCollection(Mockito.anyString(), Mockito.eq(player), Mockito.eq(10));

        queue.leavePlayer(collectionsManager, player);
        Mockito.verify(collectionsManager)
                .addCurrencyToPlayerCollection(Mockito.anyBoolean(), Mockito.anyString(), Mockito.eq(player),
                        Mockito.eq(10));
        Mockito.verifyNoMoreInteractions(collectionsManager, tournamentService);

        assertEquals(0, queue.getPlayerCount());
        assertFalse(queue.isPlayerSignedUp("p1"));
    }
    
    @Test
    public void cancellingQueue() throws SQLException, IOException {
        TournamentService tournamentService = Mockito.mock(TournamentService.class);
        ImmediateRecurringQueue queue = createQueue(tournamentService);

        User player = new User(1, "p1", "pass", "u", null, null, null, null);

        CollectionsManager collectionsManager = Mockito.mock(CollectionsManager.class);
        Mockito.when(collectionsManager.removeCurrencyFromPlayerCollection(Mockito.anyString(), Mockito.eq(player), Mockito.eq(10)))
                .thenReturn(true);

        queue.joinPlayer(collectionsManager, player, null);

        Mockito.verify(collectionsManager).removeCurrencyFromPlayerCollection(Mockito.anyString(), Mockito.eq(player), Mockito.eq(10));

        queue.leaveAllPlayers(collectionsManager);
        Mockito.verify(collectionsManager).addCurrencyToPlayerCollection(Mockito.anyBoolean(), Mockito.anyString(), Mockito.eq("p1"), Mockito.eq(10));
        Mockito.verifyNoMoreInteractions(collectionsManager, tournamentService);

        assertEquals(0, queue.getPlayerCount());
        assertFalse(queue.isPlayerSignedUp("p1"));
    }

    @Test
    public void fillingQueue() throws SQLException, IOException {
        Tournament tournament = Mockito.mock(Tournament.class);
        TournamentService tournamentService = Mockito.mock(TournamentService.class);
        Mockito.when(tournamentService
                        .addTournament(Mockito.any(ImmediateRecurringQueue.class), Mockito.anyString(),
                                Mockito.anyString()))
                .thenReturn(tournament);
        ImmediateRecurringQueue queue = createQueue(tournamentService);


        User player1 = new User(1, "p1", "pass", "u", null, null, null, null);
        User player2 = new User(2, "p2", "pass", "u", null, null, null, null);

        CollectionsManager collectionsManager = Mockito.mock(CollectionsManager.class);
        Mockito.when(collectionsManager.removeCurrencyFromPlayerCollection(Mockito.anyString(), Mockito.any(), Mockito.eq(10)))
                .thenReturn(true);

        queue.joinPlayer(collectionsManager, player1, null);

        TournamentQueueCallback queueCallback = Mockito.mock(TournamentQueueCallback.class);
        queue.process(queueCallback, collectionsManager, tournamentService);
        assertFalse(queue.shouldBeRemovedFromHall());

        Mockito.verifyNoMoreInteractions(queueCallback);

        queue.joinPlayer(collectionsManager, player2, null);

        assertEquals(2, queue.getPlayerCount());

        queue.process(queueCallback, collectionsManager, tournamentService);
        assertFalse(queue.shouldBeRemovedFromHall());

        assertEquals(0, queue.getPlayerCount());
        assertFalse(queue.isPlayerSignedUp("p1"));
        assertFalse(queue.isPlayerSignedUp("p2"));
        Mockito.verify(tournamentService)
                .addTournament(Mockito.any(ImmediateRecurringQueue.class), Mockito.anyString(), Mockito.anyString());

        Mockito.verify(tournamentService).addPlayer(Mockito.anyString(), Mockito.eq("p1"), Mockito.eq(null));
        Mockito.verify(tournamentService).addPlayer(Mockito.anyString(), Mockito.eq("p2"), Mockito.eq(null));

        Mockito.verify(queueCallback).createTournament(tournament);
        Mockito.verifyNoMoreInteractions(tournamentService, queueCallback);
    }

    @Test
    public void overflowingQueue() throws SQLException, IOException {
        Tournament tournament = Mockito.mock(Tournament.class);

        TournamentService tournamentService = Mockito.mock(TournamentService.class);
        Mockito.when(tournamentService
                        .addTournament(Mockito.any(ImmediateRecurringQueue.class), Mockito.anyString(),
                                Mockito.anyString()))
                .thenReturn(tournament);

        ImmediateRecurringQueue queue = createQueue(tournamentService);

        User player1 = new User(1, "p1", "pass", "u", null, null, null, null);
        User player2 = new User(2, "p2", "pass", "u", null, null, null, null);
        User player3 = new User(3, "p3", "pass", "u", null, null, null, null);

        CollectionsManager collectionsManager = Mockito.mock(CollectionsManager.class);
        Mockito.when(collectionsManager.removeCurrencyFromPlayerCollection(Mockito.anyString(), Mockito.any(), Mockito.eq(10)))
                .thenReturn(true);

        queue.joinPlayer(collectionsManager, player1, null);

        TournamentQueueCallback queueCallback = Mockito.mock(TournamentQueueCallback.class);
        queue.process(queueCallback, collectionsManager, tournamentService);
        assertFalse(queue.shouldBeRemovedFromHall());

        Mockito.verifyNoMoreInteractions(queueCallback);

        queue.joinPlayer(collectionsManager, player2, null);
        queue.joinPlayer(collectionsManager, player3, null);

        assertEquals(3, queue.getPlayerCount());

        queue.process(queueCallback, collectionsManager, tournamentService);
        assertFalse(queue.shouldBeRemovedFromHall());

        assertEquals(1, queue.getPlayerCount());
        assertFalse(queue.isPlayerSignedUp("p1"));
        assertFalse(queue.isPlayerSignedUp("p2"));
        assertTrue(queue.isPlayerSignedUp("p3"));

        Mockito.verify(tournamentService)
                .addTournament(Mockito.any(ImmediateRecurringQueue.class), Mockito.anyString(), Mockito.anyString());

        Mockito.verify(tournamentService).addPlayer(Mockito.anyString(), Mockito.eq("p1"), Mockito.eq(null));
        Mockito.verify(tournamentService).addPlayer(Mockito.anyString(), Mockito.eq("p2"), Mockito.eq(null));

        Mockito.verify(queueCallback).createTournament(tournament);
        Mockito.verifyNoMoreInteractions(tournamentService, queueCallback);
    }
}