package com.gempukku.stccg.filters;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

public class YouOwnNoCopiesInPlayFilter implements CardFilter {

    @JsonProperty("playerName")
    private final String _playerName;

    public YouOwnNoCopiesInPlayFilter(String playerName) {
        _playerName = playerName;
    }

    @Override
    public boolean accepts(DefaultGame game, PhysicalCard physicalCard) {
        return Filters.filterCardsInPlay(game, Filters.copyOfCard(physicalCard), Filters.owner(_playerName)).isEmpty();
    }
}