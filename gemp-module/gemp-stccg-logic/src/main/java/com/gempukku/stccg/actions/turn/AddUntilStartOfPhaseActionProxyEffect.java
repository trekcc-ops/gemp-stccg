package com.gempukku.stccg.actions.turn;

import com.gempukku.stccg.actions.ActionProxy;
import com.gempukku.stccg.actions.UnrespondableEffect;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.DefaultGame;

public class AddUntilStartOfPhaseActionProxyEffect extends UnrespondableEffect {
    private final ActionProxy _actionProxy;
    private final Phase _phase;

    public AddUntilStartOfPhaseActionProxyEffect(DefaultGame game, ActionProxy actionProxy, Phase phase) {
        super(game);
        _actionProxy = actionProxy;
        _phase = phase;
    }

    @Override
    public void doPlayEffect() {
        _game.getActionsEnvironment().addUntilStartOfPhaseActionProxy(_actionProxy, _phase);
    }
}
