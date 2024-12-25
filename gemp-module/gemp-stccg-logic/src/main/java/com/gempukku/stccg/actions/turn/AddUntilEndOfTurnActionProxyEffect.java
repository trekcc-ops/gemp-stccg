package com.gempukku.stccg.actions.turn;

import com.gempukku.stccg.gamestate.ActionProxy;
import com.gempukku.stccg.actions.UnrespondableEffect;
import com.gempukku.stccg.game.DefaultGame;

public class AddUntilEndOfTurnActionProxyEffect extends UnrespondableEffect {
    private final ActionProxy _actionProxy;

    public AddUntilEndOfTurnActionProxyEffect(DefaultGame game, ActionProxy actionProxy) {
        super(game);
        _actionProxy = actionProxy;
    }

    @Override
    public void doPlayEffect() {
        _game.getActionsEnvironment().addUntilEndOfTurnActionProxy(_actionProxy);
    }
}