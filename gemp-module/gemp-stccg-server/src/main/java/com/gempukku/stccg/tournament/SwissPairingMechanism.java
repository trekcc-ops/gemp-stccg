package com.gempukku.stccg.tournament;

import com.gempukku.stccg.competitive.PlayerStanding;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class SwissPairingMechanism implements PairingMechanism {
    private final String _registryRepresentation;
    private Integer _maxRounds;

    public SwissPairingMechanism(String registryRepresentation) {
        _registryRepresentation = registryRepresentation;
    }

    public SwissPairingMechanism(String registryRepresentation, int maxRounds) {
        this(registryRepresentation);
        _maxRounds = maxRounds;
    }

    @Override
    public String getRegistryRepresentation() {
        return _registryRepresentation;
    }

    @Override
    public String getPlayOffSystem() {
        return "Swiss";
    }

    @Override
    public boolean shouldDropLoser() {
        return false;
    }

    @Override
    public boolean isFinished(int round, Set<String> players, Set<String> droppedPlayers) {
        if (_maxRounds != null) {
            if (round >= _maxRounds)
                return true;
        }
        return round >= getRoundCountBasedOnNumberOfPlayers(players.size());
    }

    @Override
    public boolean pairPlayers(int round, Set<String> players, Set<String> droppedPlayers,
                               Map<String, Integer> playerByes, List<? extends PlayerStanding> currentStandings,
                               Map<String, ? extends Set<String>> previouslyPaired,
                               Map<? super String, ? super String> pairingResults,
                               Set<? super String> byeResults) {
        int maxNumberOfPoints = determineMaximumNumberOfPoints(droppedPlayers, currentStandings);

        List<List<String>> playersGroupedByBracket =
                groupPlayersByPointBracket(droppedPlayers, currentStandings, maxNumberOfPoints);

        shufflePlayersWithinBrackets(playersGroupedByBracket);

        Set<String> playersWithByes = getPlayersWithByes(playerByes);

        boolean success = tryPairBracketAndFurther(0, new HashSet<>(), new HashSet<>(),
                playersGroupedByBracket, playersWithByes, previouslyPaired, pairingResults, byeResults);
        // Managed to pair with this carry over count - proceed with the pairings
        return !success;

        // We can't pair, just finish the tournament
    }

    private boolean tryPairBracketAndFurther(int bracketIndex, Set<String> carryOverPlayers,
                                             Set<String> carryOverFromThisBracket,
                                             List<? extends List<String>> playersGroupedByBracket,
                                             Set<String> playersWithByes,
                                             Map<String, ? extends Set<String>> previouslyPaired,
                                             Map<? super String, ? super String> pairingsResult,
                                             Set<? super String> byes) {
        List<String> playersInBracket = playersGroupedByBracket.get(bracketIndex);

        // First try to pair carried over players
        while (!carryOverPlayers.isEmpty()) {
            String firstCarryOver = carryOverPlayers.iterator().next();
            carryOverPlayers.remove(firstCarryOver);

            for (int index = 0; index < playersInBracket.size(); index++) {
                String player = playersInBracket.remove(index);
                if (!previouslyPaired.get(firstCarryOver).contains(player))
                {
                    // This might be a good pairing
                    pairingsResult.put(firstCarryOver, player);
                    // Let's give it a try
                    boolean success = tryPairBracketAndFurther(bracketIndex, carryOverPlayers,
                            carryOverFromThisBracket, playersGroupedByBracket, playersWithByes, previouslyPaired,
                            pairingsResult, byes
                    );
                    if (success) {
                        return true;
                    }
                    // Naah, it didn't work out
                    pairingsResult.remove(firstCarryOver);
                }
                playersInBracket.add(index, player);
            }

            carryOverFromThisBracket.add(firstCarryOver);
        }

        if (playersInBracket.size() > 1) {
            // Pair whatever we manage within a bracket
            for (int index = 0; index < playersInBracket.size() - 1; index++) {
                String firstPlayer = playersInBracket.remove(index);
                for (int index2 = index; index2 < playersInBracket.size(); index2++) {
                    String secondPlayer = playersInBracket.remove(index2);
                    if (!previouslyPaired.get(firstPlayer).contains(secondPlayer))
                    {
                        // This pairing might work
                        pairingsResult.put(firstPlayer, secondPlayer);
                        // Let's give it a try
                        boolean success = tryPairBracketAndFurther(bracketIndex, Collections.emptySet(),
                                carryOverFromThisBracket, playersGroupedByBracket, playersWithByes, previouslyPaired,
                                pairingsResult, byes);
                        if (success) {
                            return true;
                        }
                        // Nah, it didn't work out
                        pairingsResult.remove(firstPlayer);
                    }
                    playersInBracket.add(index2, secondPlayer);
                }
                playersInBracket.add(index, firstPlayer);
            }
        }

        // We have to go to next bracket
        if (bracketIndex + 1 < playersGroupedByBracket.size()) {
            // Remaining players can't be paired within this bracket
            Set<String> carryOverForNextBracket = new HashSet<>(carryOverFromThisBracket);
            carryOverForNextBracket.addAll(playersInBracket);

            return tryPairBracketAndFurther(bracketIndex + 1, carryOverForNextBracket, new HashSet<>(),
                    playersGroupedByBracket, playersWithByes, previouslyPaired, pairingsResult, byes);
        } else {
            // There is no more brackets left, whatever is left, has to get a bye
            Set<String> leftoverPlayers = new HashSet<>(carryOverFromThisBracket);
            leftoverPlayers.addAll(playersInBracket);

            // We only accept one bye
            int playersLeftWithoutPair = leftoverPlayers.size();
            return switch (playersLeftWithoutPair) {
                case 0 -> true;
                case 1 -> {
                    String lastPlayer = leftoverPlayers.iterator().next();
                    if (playersWithByes.contains(lastPlayer)) {
                        // The last remaining player already has a bye
                        yield false;
                    } else {
                        byes.add(lastPlayer);
                        yield true;
                    }
                }
                default -> false;
            };
        }
    }

    private Set<String> getPlayersWithByes(Map<String, Integer> playerByes) {
        Set<String> playersWithByes = new HashSet<>();
        for (Map.Entry<String, Integer> playerByeCount : playerByes.entrySet()) {
            if (playerByeCount.getValue() != null && playerByeCount.getValue() > 0) {
                playersWithByes.add(playerByeCount.getKey());
            }
        }
        return playersWithByes;
    }

    private void shufflePlayersWithinBrackets(List<? extends List<String>> playersGroupedByPoints) {
        for (List<String> playersByPoint : playersGroupedByPoints) {
            Collections.shuffle(playersByPoint, ThreadLocalRandom.current());
        }
    }

    private List<List<String>> groupPlayersByPointBracket(Set<String> droppedPlayers,
                                                          List<? extends PlayerStanding> currentStandings,
                                                          int maxNumberOfPoints) {

        Map<Integer, List<String>> playersByPoints = new HashMap<>();
        for (PlayerStanding currentStanding : currentStandings) {
            String playerName = currentStanding.getPlayerName();
            if (!droppedPlayers.contains(playerName)) {
                int points = currentStanding.getPoints();
                List<String> playersByPoint =
                        playersByPoints.computeIfAbsent(maxNumberOfPoints - points, k -> new ArrayList<>());
                playersByPoint.add(playerName);
            }
        }
        List<List<String>> result = new ArrayList<>();
        for (int i = 0; i < maxNumberOfPoints + 2; i++) {
            List<String> playersByPoint = playersByPoints.get(i);
            if (playersByPoint != null)
                result.add(playersByPoint);
        }
        return result;
    }

    private int determineMaximumNumberOfPoints(Set<String> droppedPlayers,
                                               List<? extends PlayerStanding> currentStandings) {
        int maxNumberOfPoints = 0;
        for (PlayerStanding currentStanding : currentStandings) {
            if (!droppedPlayers.contains(currentStanding.getPlayerName())) {
                maxNumberOfPoints = Math.max(currentStanding.getPoints(), maxNumberOfPoints);
            }
        }
        return maxNumberOfPoints;
    }

    @SuppressWarnings("NonReproducibleMathCall")
    private static int getRoundCountBasedOnNumberOfPlayers(int numberOfPlayers) {
        //noinspection NumericCastThatLosesPrecision
        return (int) (Math.ceil(Math.log(numberOfPlayers) / Math.log(2)));
    }
}