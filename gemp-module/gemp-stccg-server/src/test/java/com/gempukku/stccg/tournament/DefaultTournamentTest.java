package com.gempukku.stccg.tournament;

import com.gempukku.stccg.AbstractServerTest;
import com.gempukku.stccg.common.CardDeck;
import com.gempukku.stccg.collection.CollectionsManager;
import com.gempukku.stccg.collection.CollectionType;
import com.gempukku.stccg.formats.GameFormat;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.internal.verification.Times;
import org.mockito.stubbing.Answer;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings({"LongLine", "StaticMethodReferencedViaSubclass"})
public class DefaultTournamentTest extends AbstractServerTest {

    @SuppressWarnings("unchecked") // Unchecked assignment but since it's only a test it doesn't hurt anything
    @Test
    public void testTournament() throws InterruptedException {
        TournamentService tournamentService = Mockito.mock(TournamentService.class);
        String tournamentId = "t1";
        GameFormat format = _formatLibrary.getFormatByName("1E Modern Complete");
        Map<String, CardDeck> playerDecks = new HashMap<>();
        Set<String> allPlayers = new HashSet<>(Arrays.asList("p1", "p2", "p3", "p4", "p5", "p6", "p7", "p8"));

        for (String playerName : allPlayers) {
            playerDecks.put(playerName, new CardDeck(playerName, format));
        }

        Set<String> droppedAfterRoundOne = new HashSet<>(Arrays.asList("p2", "p4", "p6", "p8"));
        Set<String> droppedAfterRoundTwo = new HashSet<>(Arrays.asList("p2", "p3", "p4", "p6", "p7", "p8"));
        Set<String> droppedAfterRoundThree = new HashSet<>(Arrays.asList("p2", "p3", "p4", "p5", "p6", "p7", "p8"));

        Mockito.when(tournamentService.getPlayers(tournamentId)).thenReturn(allPlayers);
        Mockito.when(tournamentService.getPlayerDecks(tournamentId, format.getCode())).thenReturn(playerDecks);

        PairingMechanism pairingMechanism = Mockito.mock(PairingMechanism.class);
        Mockito.when(pairingMechanism.shouldDropLoser()).thenReturn(true);

        CollectionsManager collectionsManager = Mockito.mock(CollectionsManager.class);

        DefaultTournament tournament = new DefaultTournament(tournamentService, tournamentId, "Name",
                format, CollectionType.ALL_CARDS, 0, Tournament.Stage.PLAYING_GAMES,
                pairingMechanism, new SingleEliminationOnDemandPrizes(_cardLibrary, "onDemand"));
        int _waitForPairingsTime = 100;
        tournament.setWaitForPairingsTime(_waitForPairingsTime);

        Mockito.when(pairingMechanism.isFinished(Mockito.eq(3), Mockito.eq(allPlayers), Mockito.eq(droppedAfterRoundThree)))
                .thenReturn(true);

        Mockito.when(pairingMechanism.pairPlayers(Mockito.eq(1), Mockito.eq(allPlayers), Mockito.eq(Collections.emptySet()),
                Mockito.eq(Collections.emptyMap()), Mockito.any(),
                Mockito.any(Map.class),
                Mockito.eq(Collections.emptyMap()), Mockito.eq(Collections.emptySet()))).then(
                (Answer<Boolean>) invocationOnMock -> {
                    Map<String, String> pairings = (Map<String, String>) invocationOnMock.getArguments()[6];
                    pairings.put("p1", "p2");
                    pairings.put("p3", "p4");
                    pairings.put("p5", "p6");
                    pairings.put("p7", "p8");

                    return false;
                });
        Mockito.when(pairingMechanism.pairPlayers(Mockito.eq(2), Mockito.eq(allPlayers), Mockito.eq(droppedAfterRoundOne),
                Mockito.eq(Collections.emptyMap()), Mockito.any(),
                Mockito.any(Map.class),
                Mockito.eq(Collections.emptyMap()), Mockito.eq(Collections.emptySet()))).then(
                (Answer<Boolean>) invocationOnMock -> {
                    Map<String, String> pairings = (Map<String, String>) invocationOnMock.getArguments()[6];
                    pairings.put("p1", "p3");
                    pairings.put("p5", "p7");

                    return false;
                });
        Mockito.when(pairingMechanism.pairPlayers(Mockito.eq(3), Mockito.eq(allPlayers), Mockito.eq(droppedAfterRoundTwo),
                Mockito.eq(Collections.emptyMap()), Mockito.any(),
                Mockito.any(Map.class),
                Mockito.eq(Collections.emptyMap()), Mockito.eq(Collections.emptySet()))).then(
                (Answer<Boolean>) invocationOnMock -> {
                    Map<String, String> pairings = (Map<String, String>) invocationOnMock.getArguments()[6];
                    pairings.put("p1", "p5");

                    return false;
                });

        TournamentCallback tournamentCallback = Mockito.mock(TournamentCallback.class);
        Mockito.doAnswer(
                (Answer<Void>) invocationOnMock -> {
//                    System.out.println("Broadcast: "+invocationOnMock.getArguments()[0]);
                    return null;
                }
        ).when(tournamentCallback).broadcastMessage(Mockito.anyString());

        tournament.advanceTournament(tournamentCallback, collectionsManager);

        Mockito.verify(tournamentCallback).broadcastMessage(Mockito.anyString());
        Mockito.verifyNoMoreInteractions(collectionsManager, tournamentCallback);

        Thread.sleep(_waitForPairingsTime);
        tournament.advanceTournament(tournamentCallback, collectionsManager);

        Mockito.verify(tournamentCallback, new Times(1)).createGame("p1", playerDecks.get("p1"), "p2", playerDecks.get("p2"));
        Mockito.verify(tournamentCallback, new Times(1)).createGame("p3", playerDecks.get("p3"), "p4", playerDecks.get("p4"));
        Mockito.verify(tournamentCallback, new Times(1)).createGame("p5", playerDecks.get("p5"), "p6", playerDecks.get("p6"));
        Mockito.verify(tournamentCallback, new Times(1)).createGame("p7", playerDecks.get("p7"), "p8", playerDecks.get("p8"));
        
        Mockito.verifyNoMoreInteractions(collectionsManager, tournamentCallback);

        assertEquals(1, tournament.getCurrentRound());

        tournament.advanceTournament(tournamentCallback, collectionsManager);
        Mockito.verifyNoMoreInteractions(collectionsManager, tournamentCallback);

        tournament.reportGameFinished("p1", "p2");

        tournament.advanceTournament(tournamentCallback, collectionsManager);
        Mockito.verifyNoMoreInteractions(collectionsManager, tournamentCallback);

        tournament.reportGameFinished("p3", "p4");

        tournament.advanceTournament(tournamentCallback, collectionsManager);
        Mockito.verifyNoMoreInteractions(collectionsManager, tournamentCallback);

        tournament.reportGameFinished("p5", "p6");

        tournament.advanceTournament(tournamentCallback, collectionsManager);
        Mockito.verifyNoMoreInteractions(collectionsManager, tournamentCallback);

        tournament.reportGameFinished("p7", "p8");

        tournament.advanceTournament(tournamentCallback, collectionsManager);
        Mockito.verify(tournamentCallback, new Times(2)).broadcastMessage(Mockito.anyString());
        Mockito.verifyNoMoreInteractions(collectionsManager, tournamentCallback);

        Thread.sleep(_waitForPairingsTime);
        tournament.advanceTournament(tournamentCallback, collectionsManager);

        Mockito.verify(tournamentCallback, new Times(1)).createGame("p1", playerDecks.get("p1"), "p3", playerDecks.get("p3"));
        Mockito.verify(tournamentCallback, new Times(1)).createGame("p5", playerDecks.get("p5"), "p7", playerDecks.get("p7"));

        Mockito.verifyNoMoreInteractions(collectionsManager, tournamentCallback);
        
        assertEquals(2, tournament.getCurrentRound());

        tournament.advanceTournament(tournamentCallback, collectionsManager);
        Mockito.verifyNoMoreInteractions(collectionsManager, tournamentCallback);

        tournament.reportGameFinished("p1", "p3");

        tournament.advanceTournament(tournamentCallback, collectionsManager);
        Mockito.verifyNoMoreInteractions(collectionsManager, tournamentCallback);

        tournament.reportGameFinished("p5", "p7");

        tournament.advanceTournament(tournamentCallback, collectionsManager);
        Mockito.verify(tournamentCallback, new Times(3)).broadcastMessage(Mockito.anyString());
        Mockito.verifyNoMoreInteractions(collectionsManager, tournamentCallback);

        Thread.sleep(_waitForPairingsTime);
        tournament.advanceTournament(tournamentCallback, collectionsManager);

        Mockito.verify(tournamentCallback, new Times(1)).createGame("p1", playerDecks.get("p1"), "p5", playerDecks.get("p5"));

        Mockito.verifyNoMoreInteractions(collectionsManager, tournamentCallback);

        assertEquals(3, tournament.getCurrentRound());

        tournament.advanceTournament(tournamentCallback, collectionsManager);
        Mockito.verifyNoMoreInteractions(collectionsManager, tournamentCallback);

        tournament.reportGameFinished("p1", "p5");

        tournament.advanceTournament(tournamentCallback, collectionsManager);
        Mockito.verify(tournamentCallback, new Times(4)).broadcastMessage(Mockito.anyString());

        for (String playerName : List.of("p1", "p5", "p3", "p7")) {
            Mockito.verify(collectionsManager).addItemsToPlayerMyCardsCollection(Mockito.eq(true),
                    Mockito.anyString(), Mockito.eq(playerName), Mockito.anyCollection());
        }

        assertEquals(3, tournament.getCurrentRound());
        assertEquals(Tournament.Stage.FINISHED, tournament.getTournamentStage());
    }
}