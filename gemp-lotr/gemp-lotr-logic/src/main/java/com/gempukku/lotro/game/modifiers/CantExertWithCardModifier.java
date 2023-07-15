package com.gempukku.lotro.game.modifiers;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.cards.PhysicalCard;
import com.gempukku.lotro.game.DefaultGame;

public class CantExertWithCardModifier extends AbstractModifier {
    private final Filter _preventExertWithFilter;

    public CantExertWithCardModifier(PhysicalCard source, Filterable affectFilter, Filterable preventExertWithFilter) {
        this(source, affectFilter, null, preventExertWithFilter);
    }

    public CantExertWithCardModifier(PhysicalCard source, Filterable affectFilter, Condition condition, Filterable preventExertWithFilter) {
        super(source, "Affected by exertion preventing effect", affectFilter, condition, ModifierEffect.WOUND_MODIFIER);
        _preventExertWithFilter = Filters.and(preventExertWithFilter);
    }

    @Override
    public boolean canBeExerted(DefaultGame game, PhysicalCard exertionSource, PhysicalCard exertedCard) {
        if (_preventExertWithFilter.accepts(game, exertionSource))
            return false;
        return true;
    }
}