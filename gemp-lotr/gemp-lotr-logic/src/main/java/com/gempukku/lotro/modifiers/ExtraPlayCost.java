package com.gempukku.lotro.modifiers;

import com.gempukku.lotro.cards.LotroPhysicalCard;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.actions.CostToEffectAction;
import com.gempukku.lotro.condition.Condition;

public interface ExtraPlayCost {
    void appendExtraCosts(DefaultGame game, CostToEffectAction action, LotroPhysicalCard card);

    boolean canPayExtraCostsToPlay(DefaultGame game, LotroPhysicalCard card);

    Condition getCondition();
}
