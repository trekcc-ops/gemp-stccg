package com.gempukku.stccg.effects.choose;

import com.gempukku.stccg.common.Filterable;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

import java.util.Collection;

public abstract class ChooseActiveCardEffect extends ChooseActiveCardsEffect {
    public ChooseActiveCardEffect(PhysicalCard source, String playerId, String choiceText, Filterable... filters) {
        super(source, playerId, choiceText, 1, 1, filters);
    }

    protected abstract void cardSelected(DefaultGame game, PhysicalCard card);

    @Override
    protected final void cardsSelected(DefaultGame game, Collection<PhysicalCard> cards) {
        if (cards.size() == 1)
            cardSelected(game, cards.iterator().next());
    }
}
