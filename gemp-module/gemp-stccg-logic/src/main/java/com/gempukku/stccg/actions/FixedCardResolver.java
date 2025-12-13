package com.gempukku.stccg.actions;

import com.gempukku.stccg.actions.choose.SelectCardsAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

import java.util.Collection;
import java.util.List;

public class FixedCardResolver implements ActionCardResolver {

    private final PhysicalCard _card;

    public FixedCardResolver(PhysicalCard card) {
        _card = card;
    }

    public void resolve(DefaultGame cardGame) {
    }

    public boolean isResolved() {
        return true;
    }

    @Override
    public Collection<PhysicalCard> getCards() {
        return List.of(_card);
    }

    @Override
    public SelectCardsAction getSelectionAction() {
        return null;
    }

    @Override
    public boolean cannotBeResolved(DefaultGame cardGame) {
        return false;
    }

    public PhysicalCard getCard() {
        return _card;
    }
}