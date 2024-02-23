package com.gempukku.stccg.effects.defaulteffect.unrespondable;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.effects.PreventableCardEffect;
import com.gempukku.stccg.effects.defaulteffect.UnrespondableEffect;
import com.gempukku.stccg.filters.Filter;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;

public class PreventCardEffect extends UnrespondableEffect {
    private final PreventableCardEffect _effect;
    private final Filter _filter;
    private final DefaultGame _game;

    public PreventCardEffect(DefaultGame game, PreventableCardEffect effect, Filterable... filters) {
        _effect = effect;
        _filter = Filters.and(filters);
        _game = game;
    }

    @Override
    protected void doPlayEffect() {
        for (PhysicalCard affectedCard : _effect.getAffectedCardsMinusPrevented()) {
            if (_filter.accepts(_game, affectedCard))
                _effect.preventEffect(_game, affectedCard);
        }
    }
}
