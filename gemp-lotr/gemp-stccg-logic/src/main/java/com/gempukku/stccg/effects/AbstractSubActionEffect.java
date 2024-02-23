package com.gempukku.stccg.effects;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.rules.GameUtils;

import java.util.Collection;

public abstract class AbstractSubActionEffect implements Effect {
    private CostToEffectAction _subAction;

    protected void processSubAction(DefaultGame game, CostToEffectAction subAction) {
        _subAction = subAction;
        game.getActionsEnvironment().addActionToStack(_subAction);
    }

    protected final String getAppendedNames(Collection<? extends PhysicalCard> cards) {
        return GameUtils.concatenateStrings(cards.stream().map(PhysicalCard::getCardLink));
    }

    @Override
    public boolean wasCarriedOut() {
        return _subAction != null && _subAction.wasCarriedOut();
    }
}
