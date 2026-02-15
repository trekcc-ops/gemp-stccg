package com.gempukku.stccg.tournament;

import com.gempukku.stccg.AbstractServerTest;
import com.gempukku.stccg.competitive.BestOfOneStandingsProducer;
import com.gempukku.stccg.competitive.PlayerStanding;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("MagicNumber")
public class SwissPairingMechanismTest extends AbstractServerTest {

    @Test
    public void testPairingSmallTournament() {
        int repeatCount = 10;
        int playerCount = 12;

        for (int repeat = 0; repeat < repeatCount; repeat++) {
            testSwissPairingForPlayerCount(playerCount);
        }
    }

    @Test
    public void testPairingVerySmallTournament() {
        int repeatCount = 10;
        int playerCount = 8;

        for (int repeat = 0; repeat < repeatCount; repeat++) {
            testSwissPairingForPlayerCount(playerCount);
        }
    }

    @Test
    public void testPairingSmallTournamentWithOddNumberOfPlayers() {
        int repeatCount = 10;
        int playerCount = 9;

        for (int repeat = 0; repeat < repeatCount; repeat++) {
            testSwissPairingForPlayerCount(playerCount);
        }
    }

    private void testSwissPairingForPlayerCount(int playerCount) {
        Set<String> players = new HashSet<>();
        for (int i = 0; i < playerCount; i++)
            players.add("p" + i);

        Set<String> droppedPlayers = new HashSet<>();
        Map<String, Integer> byes = new HashMap<>();

        Set<TournamentMatch> matches = new HashSet<>();
        Map<String, Set<String>> previouslyPaired = new HashMap<>();
        for (String player : players)
            previouslyPaired.put(player, new HashSet<>());

        SwissPairingMechanism pairing = new SwissPairingMechanism("swiss");
        for (int i = 1; i < 20; i++) {
            if (!pairing.isFinished(i - 1, players, droppedPlayers)) {
                List<PlayerStanding> standings =
                        BestOfOneStandingsProducer.produceStandings(players, matches, 1, 0, byes);

                Map<String, String> newPairings = new LinkedHashMap<>();
                Set<String> newByes = new HashSet<>();

                assertFalse(
                        pairing.pairPlayers(
                                i, players, droppedPlayers, byes, standings, previouslyPaired, newPairings, newByes),
                        "Unable to pair for round " + i
                );
                assertEquals(playerCount / 2, newPairings.size(), "Invalid number of pairings");
                if (playerCount % 2 == 0)
                    assertEquals(0, newByes.size(), "Invalid number of byes");
                else {
                    assertEquals(1, newByes.size(), "Invalid number of byes");
                    String newBye = newByes.iterator().next();
                    assertNull(byes.get(newBye), "Player already received bye");
                    byes.put(newBye, 1);
                }

                for (Map.Entry<String, String> newPairing : newPairings.entrySet()) {
                    String playerOne = newPairing.getKey();
                    String playerTwo = newPairing.getValue();

                    assertFalse(previouslyPaired.get(playerOne).contains(playerTwo));
                    assertFalse(previouslyPaired.get(playerTwo).contains(playerOne));

                    String winner = ThreadLocalRandom.current().nextBoolean() ? playerOne : playerTwo;

                    previouslyPaired.get(playerOne).add(playerTwo);
                    previouslyPaired.get(playerTwo).add(playerOne);

                    matches.add(new TournamentMatch(playerOne, playerTwo, winner));
                }
            }
        }
    }

}