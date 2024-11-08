package com.gempukku.stccg.processes.st1e;

import com.gempukku.stccg.game.PlayerOrder;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.processes.GameProcess;

import java.util.*;

public class ST1EPlayerOrderProcess extends ST1EGameProcess {

    public ST1EPlayerOrderProcess(ST1EGame game) {
        super(game.getPlayerIds(), game);
    }

    @Override
    public void process() {
        String[] players = _game.getAllPlayerIds();
        Map<String, Integer> diceResults = new HashMap<>();
        for (String player: players) diceResults.put(player, 0);

        while (diceResults.size() > 1) {
            for (String player : players) {
                Random rand = new Random();
                int diceRoll = rand.nextInt(6) + 1;
                _game.sendMessage(player + " rolled a " + diceRoll);
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

        _game.sendMessage(firstPlayer + " will go first");
        _game.getGameState().setPlayerOrder(new PlayerOrder(playerOrder), firstPlayer);
    }

    @Override
    public GameProcess getNextProcess() {
        return new ST1EStartOfGameProcess(_game);
    }
}