package com.gempukku.stccg.actions.usage;

import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.actions.CardPerformedAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.modifiers.LimitCounter;
import com.gempukku.stccg.player.Player;

public class UseOncePerGameAction extends ActionyAction {
    private final PhysicalCard _card;
    private final static int LIMIT_PER_GAME = 1;
    private final String _prefix;

    public UseOncePerGameAction(DefaultGame cardGame, CardPerformedAction limitedAction, PhysicalCard performingCard,
                                Player performingPlayer) {
        super(cardGame, performingPlayer, ActionType.USAGE_LIMIT);
        _card = performingCard;
        _prefix = limitedAction.getCardActionPrefix();
    }

    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        return getLimitCounter(cardGame).getUsedLimit() < LIMIT_PER_GAME;
    }

    @Override
    protected void processEffect(DefaultGame cardGame) {
        LimitCounter limitCounter = getLimitCounter(cardGame);
        limitCounter.incrementToLimit(LIMIT_PER_GAME, 1);
        setAsSuccessful();
    }

    private LimitCounter getLimitCounter(DefaultGame cardGame) {
        return cardGame.getGameState().getUntilEndOfGameLimitCounter(_card, _prefix);
    }
}