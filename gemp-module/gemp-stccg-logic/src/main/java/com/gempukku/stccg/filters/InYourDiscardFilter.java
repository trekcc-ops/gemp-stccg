package com.gempukku.stccg.filters;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;

public class InYourDiscardFilter implements CardFilter {

    private final String _playerName;

    public InYourDiscardFilter(String playerName) {
        _playerName = playerName;
    }
    @Override
    public boolean accepts(DefaultGame game, PhysicalCard physicalCard) {
        try {
            Player performingPlayer = game.getPlayer(_playerName);
            return performingPlayer.getDiscardPile().contains(physicalCard);
        } catch(PlayerNotFoundException exp) {
            game.sendErrorMessage(exp);
            return false;
        }
    }
}