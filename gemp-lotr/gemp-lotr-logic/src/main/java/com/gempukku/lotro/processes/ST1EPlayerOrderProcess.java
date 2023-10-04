package com.gempukku.lotro.processes;

import com.gempukku.lotro.game.PlayerOrder;
import com.gempukku.lotro.game.PlayerOrderFeedback;
import com.gempukku.lotro.game.ST1EGame;

import java.util.*;

public class ST1EPlayerOrderProcess implements GameProcess<ST1EGame> {
    private final PlayerOrderFeedback _playerOrderFeedback;
    private final Set<String> _players;
    private String _firstPlayer;

    public ST1EPlayerOrderProcess(Set<String> players, PlayerOrderFeedback playerOrderFeedback) {
        _players = players;
        _playerOrderFeedback = playerOrderFeedback;
    }

    @Override
    public void process(ST1EGame game) {
        Map<String, Integer> diceResults = new HashMap<>();
        for (String player: _players) diceResults.put(player, 0);

        while (diceResults.size() > 1) {
            for (String player : _players) {
                Random rand = new Random();
                int diceRoll = rand.nextInt(6) + 1;
                game.getGameState().sendMessage(player + " rolled a " + diceRoll);
                diceResults.put(player, diceRoll);
            }
            int highestRoll = Collections.max(diceResults.values());

            for (String player : _players) {
                if (diceResults.get(player) < highestRoll) {
                    diceResults.remove(player);
                }
            }
        }

        _firstPlayer = diceResults.keySet().iterator().next();

        List<String> playerOrder = new ArrayList<>();
        playerOrder.add(0, _firstPlayer);
        int playerOrderIndex = 1;
        for (String player : _players) {
            if (!Objects.equals(player, _firstPlayer)) {
                playerOrder.add(playerOrderIndex, player);
                playerOrderIndex++;
            }
        }

        game.getGameState().sendMessage(_firstPlayer + " will go first");
        _playerOrderFeedback.setPlayerOrder(new PlayerOrder(playerOrder), _firstPlayer);
    }

    @Override
    public GameProcess<ST1EGame> getNextProcess() {
        return new ST1EDoorwaySeedPhaseProcess();
    }
}
