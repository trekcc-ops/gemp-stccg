package com.gempukku.stccg.actions.usage;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.modifiers.LimitCounter;

public class UseOncePerTurnAction extends ActionyAction {
    private final PhysicalCard _card;
    private final int _limit;
    private final String _prefix;

    public UseOncePerTurnAction(Action limitedAction, PhysicalCard performingCard, Player performingPlayer) {
        super(performingPlayer, ActionType.USAGE_LIMIT);
        _card = performingCard;
        _limit = 1;
        _prefix = limitedAction.getCardActionPrefix();
    }

    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        return cardGame.getModifiersQuerying().getUntilEndOfTurnLimitCounter(_card, _prefix).getUsedLimit() < _limit;
    }

    @Override
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException {
        LimitCounter limitCounter = cardGame.getModifiersQuerying().getUntilEndOfTurnLimitCounter(_card, _prefix);
        limitCounter.incrementToLimit(_limit, 1);
        return getNextAction();
    }
}