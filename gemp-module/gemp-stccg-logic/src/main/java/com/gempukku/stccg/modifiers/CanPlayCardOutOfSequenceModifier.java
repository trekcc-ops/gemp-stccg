package com.gempukku.stccg.modifiers;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.condition.Condition;
import com.gempukku.stccg.filters.Filter;
import com.gempukku.stccg.filters.Filters;

public class CanPlayCardOutOfSequenceModifier extends AbstractModifier {

    private final Filter _filters;
    protected CanPlayCardOutOfSequenceModifier(PhysicalCard source, Condition condition, Filterable... filters) {
        super(source, null, null, condition, ModifierEffect.PLAY_OUT_OF_SEQUENCE);
        _filters = Filters.and(filters);
    }

    @Override
    public boolean canPlayCardOutOfSequence(PhysicalCard source) {
        return _condition.isFulfilled();
    }

    @Override
    public boolean affectsCard(PhysicalCard physicalCard) {
        return (_filters != null && _filters.accepts(_game, physicalCard));
    }

}
