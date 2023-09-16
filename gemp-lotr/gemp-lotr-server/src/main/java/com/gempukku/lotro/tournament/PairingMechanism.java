package com.gempukku.lotro.tournament;

import com.gempukku.lotro.competitive.PlayerStanding;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface PairingMechanism {
    boolean shouldDropLoser();

    boolean isFinished(int round, Set<String> players, Set<String> droppedPlayers);

    boolean pairPlayers(int round, Set<String> players, Set<String> droppedPlayers, Map<String, Integer> playerByes,
                               List<PlayerStanding> currentStandings, Map<String, Set<String>> previouslyPaired, Map<String, String> pairingResults, Set<String> byeResults);

    String getPlayOffSystem();

    String getRegistryRepresentation();
}
