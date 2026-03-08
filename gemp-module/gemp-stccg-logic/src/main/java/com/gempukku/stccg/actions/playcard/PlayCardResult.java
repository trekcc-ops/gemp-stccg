package com.gempukku.stccg.actions.playcard;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.actions.ActionResultType;
import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.ProxyCoreCard;
import com.gempukku.stccg.game.DefaultGame;

public class PlayCardResult extends ActionResult {
    private final PhysicalCard _playedCard;
    private final PhysicalCard _destinationCard;
    private final boolean _toCore;
    private final ActionType _actionType;

    public PlayCardResult(DefaultGame cardGame, Action action, PhysicalCard playedCard) {
        super(cardGame, ActionResultType.JUST_PLAYED, action);
        _playedCard = playedCard;
                // TODO THIS IS JUST TEMPORARY SO I CAN RUN TESTS
        _toCore = false;
        _actionType = null;
        _destinationCard = null;
    }

    public PlayCardResult(DefaultGame cardGame, Action action, PhysicalCard playedCard, PhysicalCard destinationCard,
                          ActionType actionType) {
        super(cardGame, ActionResultType.JUST_PLAYED, action);
        _actionType = actionType;
        _playedCard = playedCard;
        if (destinationCard instanceof ProxyCoreCard) {
            _destinationCard = null;
            _toCore = true;
        } else {
            _destinationCard = destinationCard;
            _toCore = false;
        }
    }


    public PhysicalCard getPlayedCard() {
        return _playedCard;
    }

    public Action getAction() { return _action; }

}