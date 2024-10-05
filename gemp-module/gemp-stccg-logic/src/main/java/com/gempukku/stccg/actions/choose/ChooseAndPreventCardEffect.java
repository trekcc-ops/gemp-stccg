package com.gempukku.stccg.actions.choose;

import com.gempukku.stccg.actions.PreventableCardEffect;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.filters.Filter;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;

public class ChooseAndPreventCardEffect extends ChooseActiveCardEffect {
    private final PreventableCardEffect _effect;

    public ChooseAndPreventCardEffect(DefaultGame game, PhysicalCard source, PreventableCardEffect effect, String playerId, String choiceText, Filterable... filters) {
        super(source, playerId, choiceText, filters);
        _effect = effect;
    }

    @Override
    protected Filter getExtraFilterForPlaying() {
        return Filters.in(_effect.getAffectedCardsMinusPrevented());
    }

    @Override
    protected void cardSelected(PhysicalCard card) {
        _effect.preventEffect(card);
    }
}
