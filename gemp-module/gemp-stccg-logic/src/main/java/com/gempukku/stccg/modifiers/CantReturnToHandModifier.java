package com.gempukku.stccg.modifiers;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.filters.CardFilter;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;

public class CantReturnToHandModifier extends AbstractModifier {
    private final CardFilter _sourceFilter;

    public CantReturnToHandModifier(PhysicalCard source, String text, Filterable affectFilter,
                                    Filterable sourceFilter) {
        super(source, text, affectFilter, ModifierEffect.RETURN_TO_HAND_MODIFIER);
        _sourceFilter = Filters.and(sourceFilter);
    }

    @Override
    public boolean canBeReturnedToHand(DefaultGame game, PhysicalCard card, PhysicalCard source) {
        return !_sourceFilter.accepts(game, source);
    }
}