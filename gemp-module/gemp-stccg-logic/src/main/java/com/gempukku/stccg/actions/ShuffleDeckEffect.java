package com.gempukku.stccg.actions;

import com.gempukku.stccg.game.DefaultGame;

public class ShuffleDeckEffect extends UnrespondableEffect {
    private final String _playerId;

    public ShuffleDeckEffect(DefaultGame game, String playerId) {
        super(game, playerId);
        _playerId = playerId;
    }

    @Override
    public void doPlayEffect() {
        _game.sendMessage(_playerId + " shuffles their deck");
        _game.getGameState().shuffleDeck(_playerId);
    }
}
