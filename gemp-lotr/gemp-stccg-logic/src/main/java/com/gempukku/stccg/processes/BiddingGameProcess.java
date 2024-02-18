package com.gempukku.stccg.processes;

import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.PlayerOrderFeedback;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class BiddingGameProcess extends GameProcess {
    private final Set<String> _players;
    private final PlayerOrderFeedback _playerOrderFeedback;
    private final Map<String, Integer> _bids = new LinkedHashMap<>();
    private final DefaultGame _game;

    public BiddingGameProcess(Set<String> players, PlayerOrderFeedback playerOrderFeedback, DefaultGame game) {
        _players = players;
        _playerOrderFeedback = playerOrderFeedback;
        _game = game;
    }

    @Override
    public void process() {
        for (String player : _players) {
            playerPlacedBid(player, 0);
        }
    }

    private void playerPlacedBid(String playerId, int bid) {
        _bids.put(playerId, bid);
    }

    @Override
    public GameProcess getNextProcess() {
        return new ChooseSeatingOrderGameProcess(_bids, _playerOrderFeedback, _game);
    }
}
