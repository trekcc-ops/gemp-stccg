package com.gempukku.lotro.effects.choose;

import com.gempukku.lotro.cards.LotroPhysicalCard;
import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.effects.ExhaustCharacterEffect;
import com.gempukku.lotro.actions.SubAction;
import com.gempukku.lotro.actions.Action;

import java.util.Collection;

public class ChooseAndExhaustCharactersEffect extends ChooseActiveCardsEffect {
    private final Action _action;

    public ChooseAndExhaustCharactersEffect(Action action, String playerId, int minimum, int maximum, Filterable... filters) {
        super(action.getActionSource(), playerId, "Choose characters to exert", minimum, maximum, filters);
        _action = action;
    }

    @Override
    protected Filter getExtraFilterForPlaying(DefaultGame game) {
        return (game1, physicalCard) -> game1.getModifiersQuerying().canBeExerted(game1, _action.getActionSource(), physicalCard)
                && game1.getModifiersQuerying().getVitality(game1, physicalCard) > 1;
    }

    @Override
    protected final void cardsSelected(DefaultGame game, Collection<LotroPhysicalCard> characters) {
        SubAction subAction = new SubAction(_action);
        subAction.appendEffect(new ExhaustCharacterEffect(_action.getActionSource(), subAction, Filters.in(characters)));
        game.getActionsEnvironment().addActionToStack(subAction);
    }
}
