package com.gempukku.stccg.modifiers;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.Filterable;
import com.gempukku.stccg.condition.Condition;
import com.gempukku.stccg.filters.Filter;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;

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
