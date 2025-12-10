package com.gempukku.stccg.actions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.choose.SelectCardsAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.filters.CardFilter;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;

import java.util.Collection;

public class AllCardsMatchingFilterResolver implements ActionCardResolver {
    private final CardFilter _cardFilter;
    private Collection<PhysicalCard> _cards;
    boolean _resolved;

    public AllCardsMatchingFilterResolver(CardFilter cardFilter) {
        _cardFilter = cardFilter;
    }

    public void resolve(DefaultGame cardGame) {
        if (!_resolved) {
            _cards = Filters.filter(cardGame, _cardFilter);
            _resolved = true;
        }
    }

    public boolean isResolved() {
        return _resolved;
    }

    public Collection<PhysicalCard> getCards(DefaultGame cardGame) throws InvalidGameLogicException {
        if (_resolved) {
            return _cards;
        } else {
            throw new InvalidGameLogicException("Have not yet resolved card target");
        }
    }

    @Override
    public SelectCardsAction getSelectionAction() {
        return null;
    }

    public boolean willProbablyBeEmpty(DefaultGame cardGame) {
        return Filters.filter(cardGame, _cardFilter).isEmpty();
    }

    public CardFilter getFilter() { return _cardFilter; }

    @JsonProperty("serialized")
    public String serialize() {
        return "filtered";
    }

}