package com.gempukku.stccg.actions.playcard;

import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.EffectResult;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;

public class PlayCardResult extends EffectResult {
    private final Zone _playedFrom;
    private final PhysicalCard _playedCard;
    private final PhysicalCard _attachedTo;
    private final PhysicalCard _attachedOrStackedPlayedFrom;
    protected final String _performingPlayerId;

    public PlayCardResult(Effect effect, Zone playedFrom, PhysicalCard playedCard) {
        this(effect, playedFrom, playedCard, null, null);
    }

    public PlayCardResult(Effect effect, Zone playedFrom, PhysicalCard playedCard,
                          PhysicalCard attachedTo, PhysicalCard attachedOrStackedPlayedFrom) {
        super(EffectResult.Type.PLAY_CARD, effect, playedCard);
        _performingPlayerId = effect.getPerformingPlayerId();
        _playedFrom = playedFrom;
        _playedCard = playedCard;
        _attachedTo = attachedTo;
        _attachedOrStackedPlayedFrom = attachedOrStackedPlayedFrom;
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

    public Zone getPlayedFrom() {
        return _playedFrom;
    }
    public String getPerformingPlayerId() { return _performingPlayerId; }
}
