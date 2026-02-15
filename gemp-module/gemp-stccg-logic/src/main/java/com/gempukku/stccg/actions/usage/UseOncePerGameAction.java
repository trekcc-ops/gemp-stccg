package com.gempukku.stccg.actions.usage;

import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.actions.blueprints.ActionBlueprint;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.modifiers.LimitCounter;

public class UseOncePerGameAction extends ActionyAction {
    private final PhysicalCard _card;
    private final static int LIMIT_PER_GAME = 1;
    private final ActionBlueprint _blueprint;

    public UseOncePerGameAction(DefaultGame cardGame, PhysicalCard performingCard,
                                String performingPlayerName, ActionBlueprint blueprint) {
        super(cardGame, performingPlayerName, ActionType.USAGE_LIMIT);
        _card = performingCard;
        _blueprint = blueprint;
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
        return cardGame.getGameState().getUntilEndOfGameLimitCounter(_performingPlayerId, _card, _blueprint);
    }
}