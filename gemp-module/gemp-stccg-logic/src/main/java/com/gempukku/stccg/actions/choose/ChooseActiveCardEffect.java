package com.gempukku.stccg.actions.choose;

import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;

import java.util.Collection;

public abstract class ChooseActiveCardEffect extends ChooseActiveCardsEffect {
    public ChooseActiveCardEffect(PhysicalCard source, String playerId, String choiceText, Filterable... filters) {
        super(source, playerId, choiceText, 1, 1, filters);
    }

    protected abstract void cardSelected(PhysicalCard card);

    @Override
    protected final void cardsSelected(Collection<PhysicalCard> cards) {
        if (cards.size() == 1)
            cardSelected(cards.iterator().next());
    }
}
