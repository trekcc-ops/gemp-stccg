package com.gempukku.stccg.filters;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

public class YouHaveNoCopiesInPlayFilter implements CardFilter {

    private final String _playerName;

    public YouHaveNoCopiesInPlayFilter(String playerName) {
        _playerName = playerName;
    }

    @Override
    public boolean accepts(DefaultGame game, PhysicalCard physicalCard) {
        return Filters.filterYourCardsInPlay(game, _playerName, Filters.copyOfCard(physicalCard)).isEmpty();
    }
}