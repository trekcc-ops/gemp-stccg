package com.gempukku.stccg.actions;

import com.gempukku.stccg.actions.choose.SelectCardsAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

import java.util.Collection;
import java.util.LinkedList;

public class FixedCardsResolver implements ActionCardResolver {

    private final Collection<PhysicalCard> _cards;

    public FixedCardsResolver(Collection<? extends PhysicalCard> cards) {
        _cards = new LinkedList<>(cards);
    }

    public void resolve(DefaultGame cardGame) {
    }

    public boolean isResolved() {
        return true;
    }

    public Collection<PhysicalCard> getCards() {
        return _cards;
    }

    @Override
    public SelectCardsAction getSelectionAction() {
        return null;
    }

    @Override
    public boolean cannotBeResolved(DefaultGame cardGame) {
        return false;
    }

}