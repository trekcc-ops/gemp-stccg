package com.gempukku.stccg.actions.playcard;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

public class PlayCardResult extends ActionResult {
    private final PhysicalCard _playedCard;
    private final PhysicalCard _attachedTo;

    public PlayCardResult(DefaultGame cardGame, Action action, PhysicalCard playedCard) {
        super(cardGame, ActionResult.Type.JUST_PLAYED, action);
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