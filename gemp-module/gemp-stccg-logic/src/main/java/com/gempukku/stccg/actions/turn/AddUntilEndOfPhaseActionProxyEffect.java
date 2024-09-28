package com.gempukku.stccg.actions.turn;

import com.gempukku.stccg.actions.ActionProxy;
import com.gempukku.stccg.actions.UnrespondableEffect;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.DefaultGame;

public class AddUntilEndOfPhaseActionProxyEffect extends UnrespondableEffect {
    private final ActionProxy _actionProxy;
    private final Phase _phase;
    private final DefaultGame _game;

    public AddUntilEndOfPhaseActionProxyEffect(DefaultGame game, ActionProxy actionProxy, Phase phase) {
        _actionProxy = actionProxy;
        _phase = phase;
        _game = game;
    }

    @Override
    public void doPlayEffect() {
        Phase phase = _phase;
        if (phase == null)
            phase = _game.getGameState().getCurrentPhase();
        _game.getActionsEnvironment().addUntilEndOfPhaseActionProxy(_actionProxy, phase);
    }
}