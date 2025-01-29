package com.gempukku.stccg.modifiers;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.filters.CardFilter;
import com.gempukku.stccg.game.DefaultGame;

public class MayNotBePlayedOnModifier extends AbstractModifier {
    private final CardFilter _unplayableCardFilter;

    public MayNotBePlayedOnModifier(PhysicalCard source, CardFilter affectFilter, CardFilter unplayableCardFilter) {
        super(source, "Affected by \"may not be played on\" limitation", affectFilter, ModifierEffect.TARGET_MODIFIER);
        _unplayableCardFilter = unplayableCardFilter;
    }

    @Override
    public boolean canHavePlayedOn(DefaultGame game, PhysicalCard playedCard, PhysicalCard target) {
        return !_unplayableCardFilter.accepts(game, playedCard);
    }
}