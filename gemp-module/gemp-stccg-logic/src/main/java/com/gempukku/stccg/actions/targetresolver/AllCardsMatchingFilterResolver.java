package com.gempukku.stccg.actions.targetresolver;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.filters.CardFilter;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

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

    public Collection<PhysicalCard> getCards() {
        return Objects.requireNonNullElseGet(_cards, ArrayList::new);
    }

    @Override
    public boolean cannotBeResolved(DefaultGame cardGame) {
        return false;
    }

    public CardFilter getFilter() { return _cardFilter; }

}