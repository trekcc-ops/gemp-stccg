package com.gempukku.stccg.modifiers;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.filters.Filter;
import com.gempukku.stccg.game.DefaultGame;

public class MayNotBePlayedOnModifier extends AbstractModifier {
    private final Filter _unplayableCardFilter;

    public MayNotBePlayedOnModifier(PhysicalCard source, Filter affectFilter, Filter unplayableCardFilter) {
        super(source, "Affected by \"may not be played on\" limitation", affectFilter, ModifierEffect.TARGET_MODIFIER);
        _unplayableCardFilter = unplayableCardFilter;
    }

    @Override
    public boolean canHavePlayedOn(DefaultGame game, PhysicalCard playedCard, PhysicalCard target) {
        return !_unplayableCardFilter.accepts(game, playedCard);
    }
}
