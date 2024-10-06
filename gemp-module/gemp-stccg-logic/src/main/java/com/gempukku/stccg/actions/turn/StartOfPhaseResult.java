package com.gempukku.stccg.actions.turn;

import com.gempukku.stccg.actions.EffectResult;
import com.gempukku.stccg.game.DefaultGame;

public class StartOfPhaseResult extends EffectResult {

    public StartOfPhaseResult(DefaultGame game) {
        super(EffectResult.Type.START_OF_PHASE, game);
    }

}
