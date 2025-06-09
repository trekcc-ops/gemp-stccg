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
    private boolean _initialized;

    public PlayOutEffectResults(DefaultGame game, ActionResult actionResult) {
        super(game);
        _actionResult = actionResult;
    }

    @Override
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException {
        if (!_initialized) {
            _initialized = true;
            _actionResult.setPlayOutAction(this);
            return _actionResult.nextAction(cardGame);
        } else {
            Action nextAction = getNextAction();
            if (nextAction == null)
                setAsSuccessful();
            return nextAction;
        }
    }

}