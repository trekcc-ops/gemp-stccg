package com.gempukku.stccg.filters;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

public class OwnedByPlayerFilter implements CardFilter {

    private final String _playerName;

    public OwnedByPlayerFilter(String playerName) {
        _playerName = playerName;
    }

    @Override
    public boolean accepts(DefaultGame game, PhysicalCard physicalCard) {
        return physicalCard.isOwnedBy(_playerName);
    }
}