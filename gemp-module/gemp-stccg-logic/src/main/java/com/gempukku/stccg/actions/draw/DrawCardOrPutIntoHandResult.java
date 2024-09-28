package com.gempukku.stccg.actions.draw;

import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.EffectResult;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;

public class DrawCardOrPutIntoHandResult extends EffectResult {
    private final String _playerId;
    private final boolean _draw;

    public DrawCardOrPutIntoHandResult(Effect effect, boolean draw) {
        super(EffectResult.Type.DRAW_CARD_OR_PUT_INTO_HAND, effect);
        _playerId = effect.getPerformingPlayerId();
        _draw = draw;
    }

    public DrawCardOrPutIntoHandResult(Effect effect, PhysicalCard card) {
        super(EffectResult.Type.DRAW_CARD_OR_PUT_INTO_HAND, card);
        _playerId = effect.getPerformingPlayerId();
        _draw = false;
    }



    public String getPlayerId() {
        return _playerId;
    }

    public boolean isDraw() {
        return _draw;
    }
}
