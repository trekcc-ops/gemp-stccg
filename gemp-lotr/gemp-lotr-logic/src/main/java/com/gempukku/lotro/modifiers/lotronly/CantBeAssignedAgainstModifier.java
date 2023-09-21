package com.gempukku.lotro.modifiers.lotronly;

import com.gempukku.lotro.cards.LotroPhysicalCard;
import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.modifiers.AbstractModifier;
import com.gempukku.lotro.condition.Condition;
import com.gempukku.lotro.modifiers.ModifierEffect;

public class CantBeAssignedAgainstModifier extends AbstractModifier {
    private final Side _side;
    private final Filter _minionFilter;

    public CantBeAssignedAgainstModifier(LotroPhysicalCard source, Side side, Filterable characterFilter, Filterable minionFilter) {
        super(source, "Is affected by assignment restriction", characterFilter, ModifierEffect.ASSIGNMENT_MODIFIER);
        _side = side;
        _minionFilter = Filters.and(minionFilter);
    }

    public CantBeAssignedAgainstModifier(LotroPhysicalCard source, Side side, Filterable characterFilter, Condition condition, Filterable minionFilter) {
        super(source, "Is affected by assignment restriction", characterFilter, condition, ModifierEffect.ASSIGNMENT_MODIFIER);
        _side = side;
        _minionFilter = Filters.and(minionFilter);
    }

}
