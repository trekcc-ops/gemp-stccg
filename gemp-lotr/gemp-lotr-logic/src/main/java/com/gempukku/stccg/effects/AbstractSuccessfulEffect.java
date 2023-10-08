package com.gempukku.stccg.effects;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.rules.GameUtils;

import java.util.Collection;

public abstract class AbstractSuccessfulEffect implements Effect {
    @Override
    public boolean isPlayableInFull(DefaultGame game) {
        return true;
    }

    @Override
    public boolean wasCarriedOut() {
        return true;
    }

    protected final String getAppendedTextNames(Collection<PhysicalCard> cards) {
        return GameUtils.getAppendedTextNames(cards);
    }

    protected final String getAppendedNames(Collection<? extends PhysicalCard> cards) {
        return GameUtils.getAppendedNames(cards);
    }
}
