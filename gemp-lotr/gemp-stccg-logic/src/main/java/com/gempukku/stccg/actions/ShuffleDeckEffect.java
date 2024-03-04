package com.gempukku.stccg.actions;

import com.gempukku.stccg.game.DefaultGame;

public class ShuffleDeckEffect extends UnrespondableEffect {
    private final String _playerId;
    private final DefaultGame _game;

    public ShuffleDeckEffect(DefaultGame game, String playerId) {
        _playerId = playerId;
        _game = game;
    }

    @Override
    public void doPlayEffect() {
        _game.getGameState().sendMessage(_playerId + " shuffles their deck");
        _game.getGameState().shuffleDeck(_playerId);
    }
}
