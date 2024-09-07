package com.gempukku.stccg.actions.turn;

import com.gempukku.stccg.actions.EffectResult;
import com.gempukku.stccg.game.DefaultGame;

public class EndOfTurnResult extends EffectResult {
    public EndOfTurnResult(DefaultGame game) {
        super(EffectResult.Type.END_OF_TURN, game);
    }
}
