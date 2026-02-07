package com.gempukku.stccg.competitive;

import com.gempukku.stccg.common.DescComparator;
import com.gempukku.stccg.common.MultipleComparator;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class BestOfOneStandingsProducer {
    private static final Comparator<PlayerStanding> LEAGUE_STANDING_COMPARATOR =
            new MultipleComparator<>(
                    new DescComparator<>(new PointsComparator()),
                    new GamesPlayedComparator(),
                    new DescComparator<>(new OpponentsWinComparator()));


    public static List<PlayerStanding> produceStandings(Collection<String> participants,
                                                        Collection<? extends CompetitiveMatchResult> matches,
                                                        int pointsForWin, int pointsForLoss,
                                                        Map<String, Integer> playersWithByes) {
        Map<String, List<String>> playerOpponents = new HashMap<>();
        Map<String, AtomicInteger> playerWinCounts = new HashMap<>();
        Map<String, AtomicInteger> playerLossCounts = new HashMap<>();

        // Initialize the list
        for (String playerName : participants) {
            playerOpponents.put(playerName, new ArrayList<>());
            playerWinCounts.put(playerName, new AtomicInteger(0));
            playerLossCounts.put(playerName, new AtomicInteger(0));
        }

        for (CompetitiveMatchResult leagueMatch : matches) {
            playerOpponents.get(leagueMatch.getWinner()).add(leagueMatch.getLoser());
            playerOpponents.get(leagueMatch.getLoser()).add(leagueMatch.getWinner());
            playerWinCounts.get(leagueMatch.getWinner()).incrementAndGet();
            playerLossCounts.get(leagueMatch.getLoser()).incrementAndGet();
        }

        List<PlayerStanding> leagueStandings = new LinkedList<>();
        for (String playerName : participants) {
            int playerWins = playerWinCounts.get(playerName).intValue();
            int playerLosses = playerLossCounts.get(playerName).intValue();
            int points = playerWins * pointsForWin + playerLosses * pointsForLoss;
            int gamesPlayed = playerWins + playerLosses;

            int byesCount = 0;
            if (playersWithByes.containsKey(playerName)) {
                byesCount = playersWithByes.get(playerName);

                points += pointsForWin * byesCount;
                gamesPlayed += byesCount;
            }

            PlayerStanding standing = new PlayerStanding(playerName, points, gamesPlayed, playerWins, byesCount);
            List<String> opponents = playerOpponents.get(playerName);
            int opponentWins = 0;
            int opponentGames = 0;
            for (String opponent : opponents) {
                opponentWins += playerWinCounts.get(opponent).intValue();
                opponentGames += playerWinCounts.get(opponent).intValue() + playerLossCounts.get(opponent).intValue();
            }
            if (opponentGames != 0) {
                standing.setOpponentWin(opponentWins * 1.0f / opponentGames);
            } else {
                standing.setOpponentWin(0.0f);
            }
            leagueStandings.add(standing);
        }

        leagueStandings.sort(LEAGUE_STANDING_COMPARATOR);

        int standing = 0;
        int position = 1;
        PlayerStanding lastStanding = null;
        for (PlayerStanding leagueStanding : leagueStandings) {
            if (lastStanding == null || LEAGUE_STANDING_COMPARATOR.compare(leagueStanding, lastStanding) != 0) {
                standing = position;
            }
            leagueStanding.setStanding(standing);
            position++;
            lastStanding = leagueStanding;
        }
        return leagueStandings;

    }

    private static class PointsComparator implements Comparator<PlayerStanding> {
        @Override
        public int compare(PlayerStanding o1, PlayerStanding o2) {
            int points1 = o1.getPoints();
            int points2 = o2.getPoints();
            return Integer.compare(points1, points2);
        }
    }

    private static class GamesPlayedComparator implements Comparator<PlayerStanding> {
        @Override
        public int compare(PlayerStanding o1, PlayerStanding o2) {
            int games1 = o1.getGamesPlayed();
            int games2 = o2.getGamesPlayed();
            return Integer.compare(games1, games2);
        }
    }

    private static class OpponentsWinComparator implements Comparator<PlayerStanding> {
        @Override
        public int compare(PlayerStanding o1, PlayerStanding o2) {
            final float diff = o1.getOpponentWin() - o2.getOpponentWin();
            if (diff < 0) {
                return -1;
            }
            if (diff > 0) {
                return 1;
            }
            return 0;
        }
    }
}