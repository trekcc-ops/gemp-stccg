package com.gempukku.stccg.actions.playcard;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;

public class PlayCardResult extends ActionResult {
    private final PhysicalCard _playedCard;
    private final PhysicalCard _attachedTo;
    private final PhysicalCard _attachedOrStackedPlayedFrom;
    protected final String _performingPlayerId;

    public PlayCardResult(Action action, PhysicalCard playedCard) {
        super(ActionResult.Type.PLAY_CARD, action, playedCard);
        _performingPlayerId = action.getPerformingPlayerId();
        _playedCard = playedCard;
        _attachedTo = null;
        _attachedOrStackedPlayedFrom = null;
    }


    public PhysicalCard getPlayedCard() {
        return _playedCard;
    }

    public PhysicalCard getAttachedTo() {
        return _attachedTo;
    }

    public PhysicalCard getAttachedOrStackedPlayedFrom() {
        return _attachedOrStackedPlayedFrom;
    }

    public String getPerformingPlayerId() { return _performingPlayerId; }
}