package com.gempukku.stccg.modifiers;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.filters.Filter;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.condition.Condition;

public class PlayersCantUseCardSpecialAbilitiesModifier extends AbstractModifier {

    private final Filter _sourceFilters;

    public PlayersCantUseCardSpecialAbilitiesModifier(PhysicalCard source, Filterable... sourceFilters) {
        this(source, null, sourceFilters);
    }

    public PlayersCantUseCardSpecialAbilitiesModifier(PhysicalCard source, Condition condition, Filterable... sourceFilters) {
        super(source, null, null, condition, ModifierEffect.ACTION_MODIFIER);
        _sourceFilters = Filters.and(sourceFilters);
    }

    @Override
    public boolean canPlayAction(DefaultGame game, String performingPlayer, Action action) {
        return action.getActionType() != Action.ActionType.SPECIAL_ABILITY
                || !_sourceFilters.accepts(game, action.getActionSource());
    }
}
