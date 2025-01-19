package com.gempukku.stccg.actions.draw;

import com.gempukku.stccg.TextUtils;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.actions.turn.AllowResponsesAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.evaluator.ConstantEvaluator;
import com.gempukku.stccg.evaluator.Evaluator;
import com.gempukku.stccg.evaluator.SkillDotCountEvaluator;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.Player;

import java.util.List;

public class DrawCardsAction extends ActionyAction implements TopLevelSelectableAction {

    private final PhysicalCard _performingCard;
    private int _cardsAlreadyDrawnCount;
    private final Evaluator _cardDrawCountEvaluator;

    public DrawCardsAction(PhysicalCard performingCard, Player performingPlayer) {
        this(performingCard, performingPlayer, false, 1);
    }

    public DrawCardsAction(PhysicalCard performingCard, Player performingPlayer,
                           SkillDotCountEvaluator drawCountEvaluator) {
        super(performingPlayer, "Draw a card", ActionType.DRAW_CARD);
        _cardDrawCountEvaluator = drawCountEvaluator;
        _performingCard = performingCard;
    }

    public DrawCardsAction(PhysicalCard performingCard, Player performingPlayer, int count) {
        this(performingCard, performingPlayer, false, count);
    }

    public DrawCardsAction(PhysicalCard performingCard, Player performingPlayer, boolean optional, int count) {
        super(performingPlayer, "Draw a card", ActionType.DRAW_CARD);
        _performingCard = performingCard;
        _cardDrawCountEvaluator = new ConstantEvaluator(performingPlayer.getGame(), count);
    }

    @Override
    public String getActionSelectionText(DefaultGame cardGame) {
        try {
            int drawCount = _cardDrawCountEvaluator.evaluateExpression(cardGame, _performingCard);
            if (drawCount == 0)
                return "Draw " + TextUtils.plural(drawCount, "card");
            else return "Draw card(s)";
        } catch(Exception exp) {
            return "Draw card(s)";
        }
    }


    @Override
    public PhysicalCard getPerformingCard() {
        return _performingCard;
    }

    @Override
    public int getCardIdForActionSelection() {
        return _performingCard.getCardId();
    }

    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        List<PhysicalCard> drawDeck = cardGame.getGameState().getDrawDeck(_performingPlayerId);
        return drawDeck.size() >= _cardDrawCountEvaluator.evaluateExpression(cardGame, _performingCard);
    }

    @Override
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException {
        int totalDrawCount = _cardDrawCountEvaluator.evaluateExpression(cardGame, _performingCard);
        if (_cardsAlreadyDrawnCount < totalDrawCount) {
            cardGame.getGameState().playerDrawsCard(_performingPlayerId);
            _cardsAlreadyDrawnCount++;
            return new AllowResponsesAction(cardGame, ActionResult.Type.DRAW_CARD);
        }
        return null;
    }

}