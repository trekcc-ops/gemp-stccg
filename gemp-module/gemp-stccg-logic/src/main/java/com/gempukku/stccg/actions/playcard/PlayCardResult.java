package com.gempukku.stccg.actions.playcard;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;

public class PlayCardResult extends ActionResult {
    private final PhysicalCard _playedCard;
    private final PhysicalCard _attachedTo;

    public PlayCardResult(Action action, PhysicalCard playedCard) {
        super(ActionResult.Type.PLAY_CARD, action);
        _playedCard = playedCard;
        _attachedTo = null;
    }


    public PhysicalCard getPlayedCard() {
        return _playedCard;
    }

    public PhysicalCard getAttachedTo() {
        return _attachedTo;
    }

    public Action getAction() { return _action; }

}