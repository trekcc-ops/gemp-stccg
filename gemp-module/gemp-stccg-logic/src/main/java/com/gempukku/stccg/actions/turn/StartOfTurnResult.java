package com.gempukku.stccg.actions.turn;

import com.gempukku.stccg.actions.EffectResult;
import com.gempukku.stccg.game.DefaultGame;

public class StartOfTurnResult extends EffectResult {
    public StartOfTurnResult() {
        super(EffectResult.Type.START_OF_TURN);
    }
}