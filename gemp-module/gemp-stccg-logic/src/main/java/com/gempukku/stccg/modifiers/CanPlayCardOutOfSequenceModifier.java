package com.gempukku.stccg.modifiers;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.condition.Condition;
import com.gempukku.stccg.filters.Filter;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;

public class CanPlayCardOutOfSequenceModifier extends AbstractModifier {

    private final Filter _filters;
    public CanPlayCardOutOfSequenceModifier(PhysicalCard source, Condition condition, Filterable... filters) {
        super(source, null, null, condition, ModifierEffect.PLAY_OUT_OF_SEQUENCE);
        _filters = Filters.and(filters);
    }

    @Override
    public boolean canPlayCardOutOfSequence(PhysicalCard source) {
        return _condition.isFulfilled(source.getGame());
    }

    @Override
    public boolean affectsCard(DefaultGame cardGame, PhysicalCard physicalCard) {
        return (_filters != null && _filters.accepts(cardGame, physicalCard));
    }


}