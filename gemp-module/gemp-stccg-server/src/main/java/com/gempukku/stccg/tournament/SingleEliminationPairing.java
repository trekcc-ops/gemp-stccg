package com.gempukku.stccg.tournament;

import com.gempukku.stccg.competitive.PlayerStanding;

import java.util.*;

public class SingleEliminationPairing implements PairingMechanism {
    private final String _registryRepresentation;

    public SingleEliminationPairing(String registryRepresentation) {

        _registryRepresentation = registryRepresentation;
    }

    @Override
    public String getRegistryRepresentation() {
        return _registryRepresentation;
    }

    @Override
    public boolean isFinished(int round, Set<String> players, Set<String> droppedPlayers) {
        return players.size() - droppedPlayers.size() < 2;
    }

    @Override
    public String getPlayOffSystem() {
        return "Single elimination";
    }

    @Override
    public boolean pairPlayers(int round, Set<String> players, Set<String> droppedPlayers,
                               Map<String, Integer> playerByes, List<? extends PlayerStanding> currentStandings,
                               Map<String, ? extends Set<String>> previouslyPaired,
                               Map<? super String, ? super String> pairingResults,
                               Set<? super String> byeResults) {

        if (isFinished(round, players, droppedPlayers))
            return true;

        Set<String> playersInContention = new HashSet<>(players);
        playersInContention.removeAll(droppedPlayers);
        int maxByes = (playerByes.isEmpty()) ? 0 : Collections.max(playerByes.values());

        Map<Integer, List<String>> playersGroupedByByes = new HashMap<>();
        for (Map.Entry<String, Integer> playerByeCount : playerByes.entrySet()) {
            String playerId = playerByeCount.getKey();
            if (playersInContention.contains(playerId)) {
                int count = playerByeCount.getValue();
                playersGroupedByByes.computeIfAbsent(count, k -> new ArrayList<>());
                playersGroupedByByes.get(count).add(playerId);
                playersInContention.remove(playerId);
            }
        }
        playersGroupedByByes.put(0, new ArrayList<>());
        for (String playerId : playersInContention)
            playersGroupedByByes.get(0).add(playerId);

        for (int i = 0; i <= maxByes; i++) {
            if (playersGroupedByByes.get(i) != null)
                Collections.shuffle(playersGroupedByByes.get(i));
        }

        int i = maxByes;
        String player1 = null;
        String player2 = null;

        while (i >= 0) {
            List<String> byePlayers = playersGroupedByByes.get(i);
            if (byePlayers == null || byePlayers.isEmpty()) {
                i--;
            } else {
                if (player1 == null) player1 = byePlayers.getFirst();
                else player2 = byePlayers.getFirst();
                playersGroupedByByes.get(i).removeFirst();
            }
            if (player1 != null && player2 != null) {
                pairingResults.put(player1, player2);
                player1 = null;
                player2 = null;
            }
        }

        if (player1 != null) byeResults.add(player1);
        return false;
    }

    @Override
    public boolean shouldDropLoser() {
        return true;
    }
}