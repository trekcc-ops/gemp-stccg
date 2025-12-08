package com.gempukku.stccg.actions.usage;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.actions.blueprints.ActionBlueprint;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.gamestate.GameState;
import com.gempukku.stccg.modifiers.LimitCounter;

public class UseOncePerTurnAction extends ActionyAction {
    private final static int LIMIT_PER_TURN = 1;
    private final ActionBlueprint _blueprint;
    private final PhysicalCard _performingCard;

    public UseOncePerTurnAction(DefaultGame cardGame, PhysicalCard performingCard, ActionBlueprint blueprint,
                                String performingPlayerName) {
        super(cardGame, performingPlayerName, ActionType.USAGE_LIMIT);
        _blueprint = blueprint;
        _performingCard = performingCard;
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
        return gameState.getUntilEndOfTurnLimitCounter(_performingCard, _blueprint);
    }
}