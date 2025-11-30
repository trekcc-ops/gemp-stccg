package com.gempukku.stccg.actions.usage;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.actions.CardPerformedAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.modifiers.LimitCounter;

public class UseOncePerTurnAction extends ActionyAction {
    private final PhysicalCard _card;
    private final static int LIMIT_PER_TURN = 1;
    private final String _prefix;

    public UseOncePerTurnAction(DefaultGame cardGame, CardPerformedAction limitedAction, PhysicalCard performingCard,
                                String performingPlayerName) {
        super(cardGame, performingPlayerName, ActionType.USAGE_LIMIT);
        _card = performingCard;
        _prefix = limitedAction.getCardActionPrefix();
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
        return cardGame.getGameState().getModifiersQuerying().getUntilEndOfTurnLimitCounter(_card, _prefix);
    }
}