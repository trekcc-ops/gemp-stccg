package com.gempukku.stccg.modifiers;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.filters.Filter;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.condition.Condition;

public class CantDiscardFromPlayModifier extends AbstractModifier {
    private final Filter _sourceFilter;

    public CantDiscardFromPlayModifier(PhysicalCard source, String text, Filterable affectFilter, Filterable sourceFilter) {
        this(source, text, null, affectFilter, sourceFilter);
    }

    public CantDiscardFromPlayModifier(PhysicalCard source, String text, Condition condition, Filterable affectFilter, Filterable sourceFilter) {
        super(source, text, affectFilter, condition, ModifierEffect.DISCARD_FROM_PLAY_MODIFIER);
        _sourceFilter = Filters.and(sourceFilter);
    }

    @Override
    public boolean canBeDiscardedFromPlay(DefaultGame game, String performingPlayer, PhysicalCard card, PhysicalCard source) {
        return !_sourceFilter.accepts(game, source);
    }
}
