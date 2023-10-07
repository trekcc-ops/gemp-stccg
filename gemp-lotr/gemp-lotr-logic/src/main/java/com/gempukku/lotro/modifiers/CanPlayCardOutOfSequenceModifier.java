package com.gempukku.lotro.modifiers;

import com.gempukku.lotro.cards.PhysicalCard;
import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.condition.Condition;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.DefaultGame;

public class CanPlayCardOutOfSequenceModifier extends AbstractModifier {

    private final Filter _filters;
    protected CanPlayCardOutOfSequenceModifier(PhysicalCard source, Condition condition, Filterable... filters) {
        super(source, null, null, condition, ModifierEffect.PLAY_OUT_OF_SEQUENCE);
        _filters = Filters.and(filters);
    }

    @Override
    public boolean canPlayCardOutOfSequence(DefaultGame game, PhysicalCard source) {
        return _condition.isFulfilled(game);
    }

    @Override
    public boolean affectsCard(DefaultGame game, PhysicalCard physicalCard) {
        return (_filters != null && _filters.accepts(game, physicalCard));
    }

}
