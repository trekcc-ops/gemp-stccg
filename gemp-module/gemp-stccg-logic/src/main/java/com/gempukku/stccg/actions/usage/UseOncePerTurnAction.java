package com.gempukku.stccg.actions.usage;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.actions.blueprints.ActionBlueprint;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.gamestate.GameState;
import com.gempukku.stccg.modifiers.LimitCounter;

public class UseOncePerTurnAction extends ActionyAction {
    private final static int LIMIT_PER_TURN = 1;
    private final ActionBlueprint _blueprint;

    public UseOncePerTurnAction(DefaultGame cardGame, ActionBlueprint blueprint, String performingPlayerName) {
        super(cardGame, performingPlayerName, ActionType.USAGE_LIMIT);
        _blueprint = blueprint;
    }


    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        return getLimitCounter(cardGame).getUsedLimit() < LIMIT_PER_TURN;
    }

    @Override
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException {
        LimitCounter limitCounter = getLimitCounter(cardGame);
        limitCounter.incrementToLimit(LIMIT_PER_TURN, 1);
        setAsSuccessful();
        return getNextAction();
    }

    private LimitCounter getLimitCounter(DefaultGame cardGame) {
        GameState gameState = cardGame.getGameState();
        return gameState.getUntilEndOfTurnLimitCounter(_blueprint);
    }
}