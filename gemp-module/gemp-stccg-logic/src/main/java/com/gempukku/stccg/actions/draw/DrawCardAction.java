package com.gempukku.stccg.actions.draw;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.actions.EffectResult;
import com.gempukku.stccg.actions.turn.AllowResponsesAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.Player;

import java.util.List;

public class DrawCardAction extends ActionyAction {

    private final PhysicalCard _performingCard;
    private final boolean _optional;
    private final int _cardDrawCount;
    private int _cardsDrawnCount;
    private boolean _optionalDecisionResolved;

    public DrawCardAction(PhysicalCard performingCard, Player performingPlayer) {
        this(performingCard, performingPlayer, false, 1);
    }

    public DrawCardAction(PhysicalCard performingCard, Player performingPlayer, int count) {
        this(performingCard, performingPlayer, false, count);
    }

    public DrawCardAction(PhysicalCard performingCard, Player performingPlayer, boolean optional, int count) {
        super(performingPlayer, "Draw a card", ActionType.DRAW_CARD);
        _performingCard = performingCard;
        _optional = optional;
        _cardDrawCount = count;
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
        List<PhysicalCard> drawDeck = cardGame.getGameState().getDrawDeck(_performingPlayerId);
        return drawDeck.size() >= _cardDrawCount;
    }

    @Override
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException {
        if (_cardsDrawnCount < _cardDrawCount) {
            cardGame.getGameState().playerDrawsCard(_performingPlayerId);
            _cardsDrawnCount++;
            return new AllowResponsesAction(cardGame, this, EffectResult.Type.DRAW_CARD);
        }
        return null;
    }
}