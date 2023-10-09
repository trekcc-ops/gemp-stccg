package com.gempukku.stccg.effects;

import com.gempukku.stccg.game.DefaultGame;

public class ShuffleDeckEffect extends UnrespondableEffect {
    private final String _playerId;

    public ShuffleDeckEffect(String playerId) {
        _playerId = playerId;
    }

    @Override
    public void doPlayEffect(DefaultGame game) {
        game.getGameState().sendMessage(_playerId + " shuffles their deck");
        game.getGameState().shuffleDeck(_playerId);
    }
}
