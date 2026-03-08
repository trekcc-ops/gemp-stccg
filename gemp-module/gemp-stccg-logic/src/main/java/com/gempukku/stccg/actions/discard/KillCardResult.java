package com.gempukku.stccg.actions.discard;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.actions.ActionResultType;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

public class KillCardResult extends ActionResult {

    private final PhysicalCard _killedCard;

    public KillCardResult(DefaultGame cardGame, Action action, PhysicalCard killedCard) {
        super(cardGame, ActionResultType.KILL_CARD, action);
        _killedCard = killedCard;
    }


    public PhysicalCard getKilledCard() { return _killedCard; }
}