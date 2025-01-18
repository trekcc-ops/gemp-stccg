package com.gempukku.stccg.actions;

import com.gempukku.stccg.actions.choose.SelectCardsAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.filters.Filter;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.google.common.collect.Iterables;

import java.util.Collection;
import java.util.LinkedList;

public class FixedCardResolver extends ActionCardResolver {

    public FixedCardResolver(PhysicalCard card) {
        super(card);
    }

    public PhysicalCard getCard() {
        return Iterables.getOnlyElement(_cards);
    }
}