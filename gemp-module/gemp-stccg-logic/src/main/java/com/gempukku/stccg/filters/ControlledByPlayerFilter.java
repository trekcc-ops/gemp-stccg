package com.gempukku.stccg.filters;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

public class ControlledByPlayerFilter implements CardFilter {

    @JsonProperty("playerName")
    private final String _playerName;

    public ControlledByPlayerFilter(String playerName) {
        _playerName = playerName;
    }
    @Override
    public boolean accepts(DefaultGame game, PhysicalCard physicalCard) {
        return physicalCard.isControlledBy(_playerName);
    }
}