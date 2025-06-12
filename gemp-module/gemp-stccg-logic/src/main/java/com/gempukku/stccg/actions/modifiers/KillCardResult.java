package com.gempukku.stccg.actions.modifiers;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;

public class KillCardResult extends ActionResult {
    private final PhysicalCard _killedCard;

    public KillCardResult(Action action, PhysicalCard killedCard) {
        super(Type.KILL_CARD, action);
        _killedCard = killedCard;
    }

    public PhysicalCard getKilledCard() { return _killedCard; }
}