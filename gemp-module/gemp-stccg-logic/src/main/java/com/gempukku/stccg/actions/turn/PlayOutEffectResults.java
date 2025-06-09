package com.gempukku.stccg.actions.turn;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.game.ActionOrder;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.gamestate.GameState;

import java.util.List;
import java.util.Set;

public class PlayOutEffectResults extends SystemQueueAction {
    private final ActionResult _actionResult;
    private final Action _action;

    public PlayOutEffectResults(DefaultGame game, Action action, ActionResult actionResult) {
        super(game);
        _actionResult = actionResult;
        _action = action;
    }

    @Override
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException {
        Action nextAction = _actionResult.nextAction(cardGame);
        if (nextAction == null) {
            setAsSuccessful();
            _action.clearResult();
        }
        return nextAction;
    }

}