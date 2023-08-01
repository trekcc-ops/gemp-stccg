package com.gempukku.lotro.effects;

import com.gempukku.lotro.actions.ActionProxy;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.game.DefaultGame;

public class AddUntilEndOfPhaseActionProxyEffect extends UnrespondableEffect {
    private final ActionProxy _actionProxy;
    private final Phase _phase;

    public AddUntilEndOfPhaseActionProxyEffect(ActionProxy actionProxy) {
        this(actionProxy, null);
    }

    public AddUntilEndOfPhaseActionProxyEffect(ActionProxy actionProxy, Phase phase) {
        _actionProxy = actionProxy;
        _phase = phase;
    }

    @Override
    public void doPlayEffect(DefaultGame game) {
        Phase phase = _phase;
        if (phase == null)
            phase = game.getGameState().getCurrentPhase();
        game.getActionsEnvironment().addUntilEndOfPhaseActionProxy(_actionProxy, phase);
    }
}