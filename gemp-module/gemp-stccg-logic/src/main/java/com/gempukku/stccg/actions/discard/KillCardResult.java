package com.gempukku.stccg.actions.discard;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionResultType;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

public class KillCardResult extends DiscardCardFromPlayResult {

    public KillCardResult(DefaultGame cardGame, Action action, PhysicalCard killedCard) {
        super(cardGame, killedCard, ActionResultType.KILL_CARD, action);
    }


    public PhysicalCard getKilledCard() { return _discardedCard; }
}