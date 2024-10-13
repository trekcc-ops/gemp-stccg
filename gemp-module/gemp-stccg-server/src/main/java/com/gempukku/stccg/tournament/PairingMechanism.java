package com.gempukku.stccg.tournament;

import com.gempukku.stccg.competitive.PlayerStanding;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface PairingMechanism {
    boolean shouldDropLoser();

    boolean isFinished(int round, Set<String> players, Set<String> droppedPlayers);

    boolean pairPlayers(int round, Set<String> players, Set<String> droppedPlayers, Map<String, Integer> playerByes,
                        List<? extends PlayerStanding> currentStandings, Map<String, ? extends Set<String>> previouslyPaired,
                        Map<? super String, ? super String> pairingResults, Set<? super String> byeResults);

    String getPlayOffSystem();

    String getRegistryRepresentation();
}