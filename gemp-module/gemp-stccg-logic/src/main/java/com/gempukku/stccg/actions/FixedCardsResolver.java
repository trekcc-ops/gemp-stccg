package com.gempukku.stccg.actions;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.choose.SelectCardsAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

import java.util.Collection;
import java.util.LinkedList;

public class FixedCardsResolver implements ActionCardResolver {

    @JsonProperty("serialized")
    @JsonIdentityReference(alwaysAsId=true)
    private final Collection<PhysicalCard> _cards;

    public FixedCardsResolver(Collection<? extends PhysicalCard> cards) {
        _cards = new LinkedList<>(cards);
    }

    public void resolve(DefaultGame cardGame) {
    }

    public boolean isResolved() {
        return true;
    }

    @Override
    public Collection<PhysicalCard> getCards(DefaultGame cardGame) {
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