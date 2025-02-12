package com.gempukku.stccg.modifiers;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.filters.CardFilter;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.condition.Condition;

public class CantDiscardFromPlayModifier extends AbstractModifier {
    private final CardFilter _sourceFilter;

    public CantDiscardFromPlayModifier(PhysicalCard source, String text, Condition condition, Filterable affectFilter,
                                       Filterable sourceFilter) {
        super(source, text, affectFilter, condition, ModifierEffect.DISCARD_FROM_PLAY_MODIFIER);
        _sourceFilter = Filters.and(sourceFilter);
    }

    @Override
    public boolean canBeDiscardedFromPlay(DefaultGame game, String performingPlayer, PhysicalCard card,
                                          PhysicalCard source) {
        return !_sourceFilter.accepts(game, source);
    }
}