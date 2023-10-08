package com.gempukku.stccg.effects.choose;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.Filterable;
import com.gempukku.stccg.filters.Filter;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.effects.PreventableCardEffect;

public class ChooseAndPreventCardEffect extends ChooseActiveCardEffect {
    private final PreventableCardEffect _effect;

    public ChooseAndPreventCardEffect(PhysicalCard source, PreventableCardEffect effect, String playerId, String choiceText, Filterable... filters) {
        super(source, playerId, choiceText, filters);
        _effect = effect;
    }

    @Override
    protected Filter getExtraFilterForPlaying(DefaultGame game) {
        return Filters.in(_effect.getAffectedCardsMinusPrevented(game));
    }

    @Override
    protected void cardSelected(DefaultGame game, PhysicalCard card) {
        _effect.preventEffect(game, card);
    }
}
