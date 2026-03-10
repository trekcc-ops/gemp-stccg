package com.gempukku.stccg.processes.st1e;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.player.PlayerNotFoundException;
import com.gempukku.stccg.player.PlayerOrder;
import com.gempukku.stccg.processes.GameProcess;

import java.util.*;

@JsonTypeName("ST1EPlayerOrderProcess")
public class ST1EPlayerOrderProcess extends ST1EGameProcess {

    @Override
    public void process(DefaultGame cardGame) throws PlayerNotFoundException {
        PlayerOrder playerOrder = getPlayerOrderByRollingDice(cardGame);
        cardGame.getGameState().initializePlayerOrder(playerOrder);
    }

    private PlayerOrder getPlayerOrderByRollingDice(DefaultGame cardGame) {
        String[] players = cardGame.getAllPlayerIds();
        Map<String, Integer> diceResults = getDiceResults(players);

        String firstPlayer = diceResults.keySet().iterator().next();

        List<String> playerOrder = new ArrayList<>();
        playerOrder.addFirst(firstPlayer);
        int playerOrderIndex = 1;
        for (String player : players) {
            if (!Objects.equals(player, firstPlayer)) {
                playerOrder.add(playerOrderIndex, player);
                playerOrderIndex++;
            }
        }
        return new PlayerOrder(playerOrder, diceResults);
    }

    private static Map<String, Integer> getDiceResults(String[] players) {
        Map<String, Integer> diceResults = new HashMap<>();
        for (String player: players) diceResults.put(player, 0);

        while (diceResults.size() > 1) {
            for (String player : players) {
                Random rand = new Random();
                int diceRoll = rand.nextInt(6) + 1;
                diceResults.put(player, diceRoll);
            }
            int highestRoll = Collections.max(diceResults.values());

            for (String player : players) {
                if (diceResults.get(player) < highestRoll) {
                    diceResults.remove(player);
                }
            }
        }
        return diceResults;
    }

    @Override
    public GameProcess getNextProcess(DefaultGame cardGame) throws InvalidGameLogicException {
        return new DoorwaySeedPhaseProcess(cardGame.getPlayerIds());
    }
}