package com.gempukku.stccg.modifiers;

import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.filters.Filter;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.condition.Condition;

public class CancelStrengthBonusTargetModifier extends AbstractModifier {
    private final Filter _sourceFilter;

    public CancelStrengthBonusTargetModifier(PhysicalCard source, Filterable affectFilter, Filterable sourceFilter) {
        this(source, null, affectFilter, sourceFilter);
    }

    public CancelStrengthBonusTargetModifier(PhysicalCard source, Condition condition, Filterable affectFilter, Filterable sourceFilter) {
        super(source, "Has some strength bonuses cancelled", affectFilter, condition, ModifierEffect.STRENGTH_BONUS_TARGET_MODIFIER);
        _sourceFilter = Filters.and(sourceFilter);
    }

    @Override
    public boolean cancelsStrengthBonusModifier(DefaultGame game, PhysicalCard modifierSource, PhysicalCard modifierTarget) {
        return _sourceFilter == null || (modifierSource != null && _sourceFilter.accepts(game, modifierSource));
    }
}
