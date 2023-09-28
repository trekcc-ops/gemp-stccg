package com.gempukku.lotro.processes;

import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.game.PlayerOrderFeedback;
import org.apache.log4j.Logger;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class BiddingGameProcess implements GameProcess {
    private static final Logger logger = Logger.getLogger(BiddingGameProcess.class);
    private final Set<String> _players;
    private final PlayerOrderFeedback _playerOrderFeedback;
    private final Map<String, Integer> _bids = new LinkedHashMap<>();

    public BiddingGameProcess(Set<String> players, PlayerOrderFeedback playerOrderFeedback) {
        _players = players;
        _playerOrderFeedback = playerOrderFeedback;
    }

    @Override
    public void process(DefaultGame game) {
        for (String player: _players) {
            logger.debug(player);
        }
        for (String player : _players) {
            playerPlacedBid(player, 0);
        }
    }

    private void playerPlacedBid(String playerId, int bid) {
        _bids.put(playerId, bid);
    }

    @Override
    public GameProcess getNextProcess() {
        return new ChooseSeatingOrderGameProcess(_bids, _playerOrderFeedback);
    }
}
