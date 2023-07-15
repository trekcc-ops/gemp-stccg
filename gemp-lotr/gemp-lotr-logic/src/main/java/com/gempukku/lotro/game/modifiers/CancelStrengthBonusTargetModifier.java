package com.gempukku.lotro.game.modifiers;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.cards.PhysicalCard;
import com.gempukku.lotro.game.DefaultGame;

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
    public boolean appliesStrengthBonusModifier(DefaultGame game, PhysicalCard modifierSource, PhysicalCard modifierTaget) {
        if (_sourceFilter == null || (modifierSource != null && _sourceFilter.accepts(game, modifierSource)))
            return false;
        return true;
    }
}