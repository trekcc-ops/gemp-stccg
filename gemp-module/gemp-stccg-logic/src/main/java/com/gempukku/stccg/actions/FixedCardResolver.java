package com.gempukku.stccg.actions;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.choose.SelectCardsAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.filters.Filter;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.google.common.collect.Iterables;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class FixedCardResolver implements ActionCardResolver {

    @JsonProperty("serialized")
    @JsonIdentityReference(alwaysAsId=true)
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
    public Collection<PhysicalCard> getCards(DefaultGame cardGame) {
        return List.of(_card);
    }

    @Override
    public SelectCardsAction getSelectionAction() {
        return null;
    }

    public boolean willProbablyBeEmpty(DefaultGame cardGame) {
        return false;
    }

    public PhysicalCard getCard() {
        return _card;
    }
}