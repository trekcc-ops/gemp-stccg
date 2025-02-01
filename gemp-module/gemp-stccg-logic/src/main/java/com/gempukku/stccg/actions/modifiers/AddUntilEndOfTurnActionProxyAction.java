package com.gempukku.stccg.actions.modifiers;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.gamestate.ActionProxy;
import com.gempukku.stccg.game.DefaultGame;

public class AddUntilEndOfTurnActionProxyAction extends ActionyAction {
    // TODO - This class not fully fleshed out
    private final ActionProxy _actionProxy;

    public AddUntilEndOfTurnActionProxyAction(DefaultGame cardGame, Player performingPlayer, ActionProxy actionProxy) {
        super(cardGame, performingPlayer, ActionType.ADD_MODIFIER);
        _actionProxy = actionProxy;
    }

    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        return false;
    }

    @Override
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException {
        cardGame.getActionsEnvironment().addUntilEndOfTurnActionProxy(_actionProxy);
        setAsSuccessful();
        return getNextAction();
    }
}