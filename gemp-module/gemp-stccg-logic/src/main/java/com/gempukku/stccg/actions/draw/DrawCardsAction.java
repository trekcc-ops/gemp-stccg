package com.gempukku.stccg.actions.draw;

import com.gempukku.stccg.actions.*;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.evaluator.ConstantEvaluator;
import com.gempukku.stccg.evaluator.Evaluator;
import com.gempukku.stccg.evaluator.SkillDotCountEvaluator;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;

public class DrawCardsAction extends ActionyAction implements TopLevelSelectableAction {

    private final PhysicalCard _performingCard;
    private int _cardsAlreadyDrawnCount;
    private final Evaluator _cardDrawCountEvaluator;

    public DrawCardsAction(PhysicalCard performingCard, Player performingPlayer) {
        this(performingCard.getGame(), performingCard, performingPlayer, false, 1);
    }

    public DrawCardsAction(DefaultGame cardGame, PhysicalCard performingCard, String performingPlayerName,
                           boolean optional, int count) {
        super(cardGame, performingPlayerName, "Draw a card", ActionType.DRAW_CARD);
        _performingCard = performingCard;
        _cardDrawCountEvaluator = new ConstantEvaluator(count);
    }

    public DrawCardsAction(DefaultGame cardGame, PhysicalCard performingCard, String performingPlayerName,
                           SkillDotCountEvaluator drawCountEvaluator) {
        super(cardGame, performingPlayerName, "Draw a card", ActionType.DRAW_CARD);
        _cardDrawCountEvaluator = drawCountEvaluator;
        _performingCard = performingCard;
    }

    public DrawCardsAction(DefaultGame cardGame, PhysicalCard performingCard, Player performingPlayer,
                           SkillDotCountEvaluator drawCountEvaluator) {
        super(cardGame, performingPlayer, "Draw a card", ActionType.DRAW_CARD);
        _cardDrawCountEvaluator = drawCountEvaluator;
        _performingCard = performingCard;
    }

    public DrawCardsAction(PhysicalCard performingCard, String performingPlayerName, int count, DefaultGame cardGame) {
        this(cardGame, performingCard, performingPlayerName, false, count);
    }

    public DrawCardsAction(PhysicalCard performingCard, Player performingPlayer, int count, DefaultGame cardGame) {
        this(cardGame, performingCard, performingPlayer, false, count);
    }

    public DrawCardsAction(DefaultGame cardGame, PhysicalCard performingCard, Player performingPlayer,
                           boolean optional, int count) {
        this(cardGame, performingCard, performingPlayer.getPlayerId(), optional, count);
    }

    @Override
    public PhysicalCard getPerformingCard() {
        return _performingCard;
    }

    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        try {
            Player performingPlayer = cardGame.getPlayer(_performingPlayerId);
            return performingPlayer.getCardsInDrawDeck().size() >= (int) _cardDrawCountEvaluator.evaluateExpression(cardGame);
        } catch(PlayerNotFoundException exp) {
            cardGame.sendErrorMessage(exp);
            return false;
        }
    }

    @Override
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException, PlayerNotFoundException {
        int totalDrawCount = (int) _cardDrawCountEvaluator.evaluateExpression(cardGame);
        if (isBeingInitiated()) {
            setAsInitiated();
        }
        if (_cardsAlreadyDrawnCount < totalDrawCount) {
            _cardsAlreadyDrawnCount++;
            return new DrawSingleCardAction(cardGame, cardGame.getPlayer(_performingPlayerId));
        } else {
            setAsSuccessful();
        }
        return null;
    }

}