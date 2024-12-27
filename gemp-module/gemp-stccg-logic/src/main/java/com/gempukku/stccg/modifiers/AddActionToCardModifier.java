package com.gempukku.stccg.modifiers;

import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.condition.Condition;
import com.gempukku.stccg.actions.turn.ActivateCardAction;

public abstract class AddActionToCardModifier extends AbstractModifier {
    public AddActionToCardModifier(PhysicalCard source, Condition condition, Filterable... affectFilter) {
        super(source, "Has extra action from " + source.getFullName(), Filters.and(affectFilter), condition, ModifierEffect.EXTRA_ACTION_MODIFIER);
    }

    protected abstract ActivateCardAction createExtraPhaseAction(DefaultGame game, PhysicalCard matchingCard);
}