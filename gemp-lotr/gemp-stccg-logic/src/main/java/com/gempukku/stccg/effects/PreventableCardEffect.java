package com.gempukku.stccg.effects;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

import java.util.Collection;

public interface PreventableCardEffect {
    Collection<PhysicalCard> getAffectedCardsMinusPrevented(DefaultGame game);

    void preventEffect(DefaultGame game, PhysicalCard affectedCard);
}
