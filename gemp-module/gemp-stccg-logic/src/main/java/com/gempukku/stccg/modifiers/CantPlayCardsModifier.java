package com.gempukku.stccg.modifiers;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.filters.Filter;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.condition.Condition;

public class CantPlayCardsModifier extends AbstractModifier {
    private final Filter _filters;

    public CantPlayCardsModifier(PhysicalCard source, Filterable... filters) {
        this(source, null, filters);
    }

    public CantPlayCardsModifier(PhysicalCard source, Condition condition, Filterable... filters) {
        super(source, null, null, condition, ModifierEffect.ACTION_MODIFIER);
        _filters = Filters.and(filters);
    }

    @Override
    public boolean cantPlayCard(DefaultGame game, String performingPlayer, PhysicalCard card) {
        return _filters.accepts(game, card);
    }

    @Override
    public boolean canPlayAction(DefaultGame game, String performingPlayer, Action action) {
        final PhysicalCard actionSource = action.getActionSource();
        if (actionSource != null)
            if (action.getActionType() == Action.ActionType.PLAY_CARD)
                return !_filters.accepts(game, actionSource);
        return true;
    }
}
