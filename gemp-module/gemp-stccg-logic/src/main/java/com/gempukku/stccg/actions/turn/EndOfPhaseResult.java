package com.gempukku.stccg.actions.turn;

import com.gempukku.stccg.actions.EffectResult;
import com.gempukku.stccg.game.DefaultGame;

public class EndOfPhaseResult extends EffectResult {
    public EndOfPhaseResult(DefaultGame game) {
        super(EffectResult.Type.END_OF_PHASE, game);
    }

}
