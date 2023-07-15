package com.gempukku.lotro.game.modifiers;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.cards.PhysicalCard;
import com.gempukku.lotro.game.DefaultGame;

public class CantHealModifier extends AbstractModifier {
    public CantHealModifier(PhysicalCard source, Filterable... affectFilters) {
        this(source, null, affectFilters);
    }

    public CantHealModifier(PhysicalCard source, Condition condition, Filterable... affectFilters) {
        super(source, "Can't be healed", Filters.and(affectFilters), condition, ModifierEffect.WOUND_MODIFIER);
    }

    @Override
    public boolean canBeHealed(DefaultGame game, PhysicalCard card) {
        return false;
    }
}