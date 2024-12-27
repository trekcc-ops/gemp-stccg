package com.gempukku.stccg.actions.draw;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.EffectResult;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;

public class DrawCardOrPutIntoHandResult extends EffectResult {
    private final String _playerId;
    private final boolean _draw;

    public DrawCardOrPutIntoHandResult(Action action, PhysicalCard performingCard, boolean draw) {
        super(EffectResult.Type.DRAW_CARD_OR_PUT_INTO_HAND, action, performingCard);
        _playerId = action.getPerformingPlayerId();
        _draw = draw;
    }


    public String getPlayerId() {
        return _playerId;
    }

    public boolean isDraw() {
        return _draw;
    }
}