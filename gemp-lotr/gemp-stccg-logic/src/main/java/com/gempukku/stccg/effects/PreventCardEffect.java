package com.gempukku.stccg.effects;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.filters.Filter;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;

public class PreventCardEffect extends UnrespondableEffect {
    private final PreventableCardEffect _effect;
    private final Filter _filter;

    public PreventCardEffect(PreventableCardEffect effect, Filterable... filters) {
        _effect = effect;
        _filter = Filters.and(filters);
    }

    @Override
    protected void doPlayEffect(DefaultGame game) {
        for (PhysicalCard affectedCard : _effect.getAffectedCardsMinusPrevented(game)) {
            if (_filter.accepts(game, affectedCard))
                _effect.preventEffect(game, affectedCard);
        }
    }
}
