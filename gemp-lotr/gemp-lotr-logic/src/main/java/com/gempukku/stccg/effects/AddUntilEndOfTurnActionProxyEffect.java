package com.gempukku.stccg.effects;

import com.gempukku.stccg.actions.ActionProxy;
import com.gempukku.stccg.game.DefaultGame;

public class AddUntilEndOfTurnActionProxyEffect extends UnrespondableEffect {
    private final ActionProxy _actionProxy;

    public AddUntilEndOfTurnActionProxyEffect(ActionProxy actionProxy) {
        _actionProxy = actionProxy;
    }

    @Override
    public void doPlayEffect(DefaultGame game) {
        game.getActionsEnvironment().addUntilEndOfTurnActionProxy(_actionProxy);
    }
}