package com.gempukku.stccg.modifiers;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.requirement.Condition;
import com.gempukku.stccg.game.DefaultGame;

public interface ExtraPlayCost {
    void appendExtraCosts(DefaultGame game, Action action, PhysicalCard card);

    boolean canPayExtraCostsToPlay(PhysicalCard card);

    Condition getCondition();
}