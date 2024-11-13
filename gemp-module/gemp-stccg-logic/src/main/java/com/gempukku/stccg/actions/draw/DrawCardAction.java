package com.gempukku.stccg.actions.draw;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.Player;

public class DrawCardAction extends ActionyAction {

    private final PhysicalCard _performingCard;
    private final boolean _optional;

    public DrawCardAction(PhysicalCard performingCard, Player performingPlayer) {
        this(performingCard, performingPlayer, false);
    }

    public DrawCardAction(PhysicalCard performingCard, Player performingPlayer, boolean optional) {
        super(performingPlayer, "Draw a card", ActionType.DRAW_CARD);
        _performingCard = performingCard;
        _optional = optional;
    }


    @Override
    public PhysicalCard getActionSource() {
        return _performingCard;
    }

    @Override
    public PhysicalCard getCardForActionSelection() {
        return _performingCard;
    }

    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        return !cardGame.getGameState().getDrawDeck(_performingPlayerId).isEmpty();
    }

    @Override
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException {
        cardGame.getGameState().playerDrawsCard(_performingPlayerId);
        cardGame.getActionsEnvironment().emitEffectResult(
                new DrawCardOrPutIntoHandResult(this, _performingCard, true));
        return getNextAction();
    }
}