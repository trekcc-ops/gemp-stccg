package com.gempukku.stccg.actions.discard;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

public class DiscardCardFromHandResult extends ActionResult {
    private final PhysicalCard _card;

    public DiscardCardFromHandResult(PhysicalCard card, Action action, DefaultGame cardGame) {
        super(Type.FOR_EACH_DISCARDED_FROM_HAND, action.getPerformingPlayerId(), action);
        _card = card;
    }

    public PhysicalCard getDiscardedCard() {
        return _card;
    }
}