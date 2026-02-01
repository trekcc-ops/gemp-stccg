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

@SuppressWarnings({"LongLine", "StaticMethodReferencedViaSubclass", "unchecked"})
public class DefaultTournamentTest extends AbstractServerTest {

    Map<String, CardDeck> playerDecks = new HashMap<>();
    Set<String> allPlayers = new HashSet<>(Arrays.asList("p1", "p2", "p3", "p4", "p5", "p6", "p7", "p8"));
    Set<String> droppedPlayers = new HashSet<>();
    int _waitForPairingsTime = 100;
    CollectionsManager collectionsManager = Mockito.mock(CollectionsManager.class);

    Map<Integer, Map<String, String>> pairingsByRound = Map.of(
    1, Map.of("p1", "p2", "p3", "p4", "p5", "p6", "p7", "p8"),
    2, Map.of("p1", "p3", "p5", "p7"),
            3, Map.of("p1", "p5")
    );

    @Test
    public void testTournament() throws InterruptedException {
        TournamentService tournamentService = Mockito.mock(TournamentService.class);
        String tournamentId = "t1";
        GameFormat format = _formatLibrary.getFormatByName("1E Modern Complete");

        for (String playerName : allPlayers) {
            playerDecks.put(playerName, new CardDeck(playerName, format));
        }

        Mockito.when(tournamentService.getPlayers(tournamentId)).thenReturn(allPlayers);
        Mockito.when(tournamentService.getPlayerDecks(tournamentId, format.getCode())).thenReturn(playerDecks);

        PairingMechanism pairingMechanism = Mockito.mock(PairingMechanism.class);
        Mockito.when(pairingMechanism.shouldDropLoser()).thenReturn(true);

        DefaultTournament tournament = new DefaultTournament(tournamentService, tournamentId, "Name",
                format, CollectionType.ALL_CARDS, 0, Tournament.Stage.PLAYING_GAMES,
                pairingMechanism, new SingleEliminationOnDemandPrizes(_cardLibrary, "onDemand"));
        tournament.setWaitForPairingsTime(_waitForPairingsTime);

        TournamentCallback tournamentCallback = Mockito.mock(TournamentCallback.class);
        Mockito.doAnswer(
                (Answer<Void>) invocationOnMock -> {
//                    System.out.println("Broadcast: "+invocationOnMock.getArguments()[0]);
                    return null;
                }
        ).when(tournamentCallback).broadcastMessage(Mockito.anyString());

        tournament.advanceTournament(tournamentCallback, collectionsManager);

        Mockito.verify(tournamentCallback).broadcastMessage(Mockito.anyString());

        Mockito.when(pairingMechanism.isFinished(Mockito.eq(3), Mockito.eq(allPlayers), Mockito.eq(droppedPlayers)))
                .thenReturn(true);

        processRound(pairingMechanism, tournament, tournamentCallback, 1);
        processRound(pairingMechanism, tournament, tournamentCallback, 2);
        processRound(pairingMechanism, tournament, tournamentCallback, 3);

        for (String playerName : List.of("p1", "p5", "p3", "p7")) {
            Mockito.verify(collectionsManager).addItemsToPlayerMyCardsCollection(Mockito.eq(true),
                    Mockito.anyString(), Mockito.eq(playerName), Mockito.anyCollection());
        }

        assertEquals(3, tournament.getCurrentRound());
        assertEquals(Tournament.Stage.FINISHED, tournament.getTournamentStage());
    }

    private void processRound(PairingMechanism pairingMechanism, DefaultTournament tournament,
                              TournamentCallback tournamentCallback,
                              int roundNumber) throws InterruptedException {

        Map<String, String> roundPairings = pairingsByRound.get(roundNumber);
        Mockito.verifyNoMoreInteractions(collectionsManager, tournamentCallback);

        Mockito.when(pairingMechanism.pairPlayers(Mockito.eq(roundNumber), Mockito.eq(allPlayers),
                Mockito.eq(droppedPlayers), Mockito.eq(Collections.emptyMap()), Mockito.any(),
                Mockito.any(Map.class),
                Mockito.eq(Collections.emptyMap()), Mockito.eq(Collections.emptySet()))).then(
                (Answer<Boolean>) invocationOnMock -> {
                    Map<String, String> pairings = (Map<String, String>) invocationOnMock.getArguments()[6];
                    pairings.putAll(roundPairings);
                    return false;
                });

        Thread.sleep(_waitForPairingsTime);
        tournament.advanceTournament(tournamentCallback, collectionsManager);

        for (Map.Entry<String, String> pairing : roundPairings.entrySet()) {
            String player1 = pairing.getKey();
            String player2 = pairing.getValue();
            Mockito.verify(tournamentCallback, new Times(1))
                    .createGame(player1, playerDecks.get(player1), player2, playerDecks.get(player2));
        }
        Mockito.verifyNoMoreInteractions(collectionsManager, tournamentCallback);

        assertEquals(roundNumber, tournament.getCurrentRound());

        for (Map.Entry<String, String> pairing : roundPairings.entrySet()) {
            tournament.advanceTournament(tournamentCallback, collectionsManager);
            Mockito.verifyNoMoreInteractions(collectionsManager, tournamentCallback);
            String winner = pairing.getKey();
            String loser = pairing.getValue();
            tournament.reportGameFinished(winner, loser);
            droppedPlayers.add(loser);
        }

        tournament.advanceTournament(tournamentCallback, collectionsManager);
        Mockito.verify(tournamentCallback,
                new Times(roundNumber + 1)).broadcastMessage(Mockito.anyString());
    }

}