package com.gempukku.stccg.actions;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;

import com.gempukku.stccg.filters.Filter;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;

public class PreventCardEffect extends UnrespondableEffect {
    private final PreventableCardEffect _effect;
    private final Filter _filter;

    public PreventCardEffect(DefaultGame game, PreventableCardEffect effect, Filterable... filters) {
        super(game);
        _effect = effect;
        _filter = Filters.and(filters);
    }

    @Override
    protected void doPlayEffect() {
        for (PhysicalCard affectedCard : _effect.getAffectedCardsMinusPrevented()) {
            if (_filter.accepts(_game, affectedCard))
                _effect.preventEffect(affectedCard);
        }
    }
}
