package com.gempukku.stccg.actions.playcard;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;

public class PlayCardInitiationResult extends ActionResult {
    private final PhysicalCard _cardToPlay;
    private final ActionyAction _playCardAction;

    public PlayCardInitiationResult(ActionyAction action, PhysicalCard cardToPlay) {
        super(Type.PLAY_CARD_INITIATION, action);
        _cardToPlay = cardToPlay;
        _playCardAction = action;
    }

    public PhysicalCard getCardToPlay() { return _cardToPlay; }

    public ActionyAction getAction() { return _playCardAction; }
}