package com.gempukku.stccg.processes.st1e;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.gempukku.stccg.game.*;
import com.gempukku.stccg.player.PlayerNotFoundException;
import com.gempukku.stccg.player.PlayerOrder;
import com.gempukku.stccg.processes.GameProcess;

import java.util.*;

@JsonTypeName("ST1EPlayerOrderProcess")
public class ST1EPlayerOrderProcess extends ST1EGameProcess {

    @Override
    public void process(DefaultGame cardGame) throws PlayerNotFoundException {
        List<String> playerOrder;
        if (cardGame.getFormat().hasFixedPlayerOrder()) {
            playerOrder = Arrays.asList(cardGame.getAllPlayerIds());
        } else {
            playerOrder = getPlayerOrderByRollingDice(cardGame);
        }
        cardGame.sendMessage(playerOrder.getFirst() + " will go first");
        cardGame.getGameState().initializePlayerOrder(new PlayerOrder(playerOrder));
    }

    private List<String> getPlayerOrderByRollingDice(DefaultGame cardGame) {
        String[] players = cardGame.getAllPlayerIds();
        Map<String, Integer> diceResults = new HashMap<>();
        for (String player: players) diceResults.put(player, 0);

        while (diceResults.size() > 1) {
            for (String player : players) {
                Random rand = new Random();
                int diceRoll = rand.nextInt(6) + 1;
                cardGame.sendMessage(player + " rolled a " + diceRoll);
                diceResults.put(player, diceRoll);
            }
            int highestRoll = Collections.max(diceResults.values());

            for (String player : players) {
                if (diceResults.get(player) < highestRoll) {
                    diceResults.remove(player);
                }
            }
        }

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
        return playerOrder;
    }

    @Override
    public GameProcess getNextProcess(DefaultGame cardGame) throws InvalidGameLogicException {
        return new DoorwaySeedPhaseProcess(cardGame.getPlayerIds());
    }
}