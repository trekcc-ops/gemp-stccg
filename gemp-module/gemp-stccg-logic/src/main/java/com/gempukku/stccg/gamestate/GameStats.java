package com.gempukku.stccg.gamestate;

import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.PlayerOrder;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

//  Game stats to be delivered to the client

public class GameStats {
    private Map<String, Map<Zone, Integer>> _zoneSizes;
    private Map<String, Integer> _playerScores;

    public GameStats() {
        _zoneSizes = new HashMap<>();
        _playerScores = new HashMap<>();
    }

    public GameStats(GameStats originalStats) {
        _zoneSizes = new HashMap<>(originalStats._zoneSizes);
        _playerScores = new HashMap<>(originalStats._playerScores);
    }

    /**
     * @return If the stats have changed
     */
    public boolean updateGameStats(DefaultGame game) {
        boolean changed = false;
        PlayerOrder playerOrder = game.getGameState().getPlayerOrder();

        Map<String, Map<Zone, Integer>> newZoneSizes = new HashMap<>();
        if (playerOrder != null) {
            for (String player : playerOrder.getAllPlayers()) {
                final HashMap<Zone, Integer> playerZoneSizes = new HashMap<>();
                playerZoneSizes.put(Zone.HAND, game.getGameState().getHand(player).size());
                playerZoneSizes.put(Zone.DRAW_DECK, game.getGameState().getDrawDeck(player).size());
                playerZoneSizes.put(Zone.DISCARD, game.getGameState().getDiscard(player).size());
                playerZoneSizes.put(Zone.REMOVED, game.getGameState().getRemoved(player).size());
                newZoneSizes.put(player, playerZoneSizes);
            }
        }

        if (!newZoneSizes.equals(_zoneSizes)) {
            changed = true;
            _zoneSizes = newZoneSizes;
        }

        Map<String, Integer> newPlayerScores = new HashMap<>();
        if (playerOrder != null) {
            for (String player : playerOrder.getAllPlayers()) {
                newPlayerScores.put(player, game.getGameState().getPlayerScore(player));
            }
        }

        if (!newPlayerScores.equals(_playerScores)) {
            changed = true;
            _playerScores = newPlayerScores;
        }

        return changed;
    }

    public Map<String, Map<Zone, Integer>> getZoneSizes() {
        return Collections.unmodifiableMap(_zoneSizes);
    }

    public Map<String, Integer> getPlayerScores() { return Collections.unmodifiableMap(_playerScores); }

}
