package com.gempukku.stccg.actions.discard;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionResultType;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

import java.util.List;

public class KillCardResult extends DiscardCardFromPlayResult {

    public KillCardResult(DefaultGame cardGame, Action action, PhysicalCard killedCard) {
        super(cardGame, killedCard, List.of(ActionResultType.KILL_CARD, ActionResultType.JUST_DISCARDED_FROM_PLAY), action);
    }


    public PhysicalCard getKilledCard() { return _discardedCard; }
}