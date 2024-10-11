package com.gempukku.stccg.modifiers;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.condition.Condition;

public interface ExtraPlayCost {
    void appendExtraCosts(DefaultGame game, CostToEffectAction action, PhysicalCard card);

    boolean canPayExtraCostsToPlay(PhysicalCard card);

    Condition getCondition();
}
