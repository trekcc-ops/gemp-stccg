package com.gempukku.stccg.modifiers;

import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.condition.Condition;
import com.gempukku.stccg.rules.GameUtils;
import com.gempukku.stccg.actions.ActivateCardAction;

import java.util.Collections;
import java.util.List;

public abstract class AddActionToCardModifier extends AbstractModifier {
    public AddActionToCardModifier(PhysicalCard source, Condition condition, Filterable... affectFilter) {
        super(source, "Has extra action from " + source.getFullName(), Filters.and(affectFilter), condition, ModifierEffect.EXTRA_ACTION_MODIFIER);
    }

    @Override
    public List<? extends ActivateCardAction> getExtraPhaseAction(DefaultGame game, PhysicalCard card) {
        final ActivateCardAction extraPhaseAction = createExtraPhaseAction(game, card);
        if (extraPhaseAction != null)
            return Collections.singletonList(extraPhaseAction);
        return null;
    }

    protected abstract ActivateCardAction createExtraPhaseAction(DefaultGame game, PhysicalCard matchingCard);
}
