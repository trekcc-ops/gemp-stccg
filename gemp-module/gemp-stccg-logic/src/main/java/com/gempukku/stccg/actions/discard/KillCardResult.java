package com.gempukku.stccg.actions.discard;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;

import java.util.List;

public class KillCardResult extends DiscardCardFromPlayResult {

    public KillCardResult(Action action, PhysicalCard killedCard) {
        super(killedCard, List.of(Type.KILL_CARD, Type.JUST_DISCARDED_FROM_PLAY), action);
    }


    public PhysicalCard getKilledCard() { return _discardedCard; }
}