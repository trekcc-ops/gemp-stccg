package com.gempukku.stccg.actions;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

import java.util.Collection;

public interface ActionCardResolver extends ActionTargetResolver {
    Collection<PhysicalCard> getCards();
    default Collection<PhysicalCard> getCards(DefaultGame cardGame) {
        return getCards();
    }
}