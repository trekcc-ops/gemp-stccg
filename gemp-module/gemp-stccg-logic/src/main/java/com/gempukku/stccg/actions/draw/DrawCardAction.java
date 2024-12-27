package com.gempukku.stccg.actions.draw;

import com.gempukku.stccg.TextUtils;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.actions.turn.AllowResponsesAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.evaluator.SkillDotCountEvaluator;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.Player;

import java.util.List;

public class DrawCardAction extends ActionyAction {

    private final PhysicalCard _performingCard;
    private final boolean _optional;
    private int _cardDrawCount;
    private int _cardsDrawnCount;
    private SkillDotCountEvaluator _cardDrawCountEvaluator;
    private boolean _cardDrawCountResolved = false;
    private boolean _optionalDecisionResolved;

    public DrawCardAction(PhysicalCard performingCard, Player performingPlayer) {
        this(performingCard, performingPlayer, false, 1);
    }

    public DrawCardAction(PhysicalCard performingCard, Player performingPlayer,
                          SkillDotCountEvaluator drawCountEvaluator) {
        super(performingPlayer, "Draw a card", ActionType.DRAW_CARD);
        _optional = false;
        _cardDrawCountEvaluator = drawCountEvaluator;
        _performingCard = performingCard;
    }

    public DrawCardAction(PhysicalCard performingCard, Player performingPlayer, int count) {
        this(performingCard, performingPlayer, false, count);
    }

    public DrawCardAction(PhysicalCard performingCard, Player performingPlayer, boolean optional, int count) {
        super(performingPlayer, "Draw a card", ActionType.DRAW_CARD);
        _performingCard = performingCard;
        _optional = optional;
        _cardDrawCount = count;
        _cardDrawCountResolved = true;
    }

    @Override
    public String getActionSelectionText(DefaultGame cardGame) {
        int drawCount;
        if (_cardDrawCountResolved) {
            drawCount = _cardDrawCount;
        } else {
            try {
                drawCount = _cardDrawCountEvaluator.evaluateExpression(cardGame);
            } catch(Exception exp) {
                return "Draw card(s)";
            }
            if (drawCount == 0)
                return "Draw card(s)";
        }
        return "Draw " + TextUtils.plural(drawCount, "card");
    }


    @Override
    public PhysicalCard getPerformingCard() {
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
        if (!_cardDrawCountResolved) {
            _cardDrawCount = _cardDrawCountEvaluator.evaluateExpression(cardGame);
            _cardDrawCountResolved = true;
        }

        if (_cardsDrawnCount < _cardDrawCount) {
            cardGame.getGameState().playerDrawsCard(_performingPlayerId);
            _cardsDrawnCount++;
            return new AllowResponsesAction(cardGame, ActionResult.Type.DRAW_CARD);
        }
        return null;
    }
}