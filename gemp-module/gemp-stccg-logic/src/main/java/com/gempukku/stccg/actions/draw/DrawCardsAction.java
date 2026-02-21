package com.gempukku.stccg.actions.draw;

import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.decisions.AwaitingDecision;
import com.gempukku.stccg.decisions.DecisionContext;
import com.gempukku.stccg.decisions.IntegerAwaitingDecision;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;

public class DrawCardsAction extends ActionyAction implements TopLevelSelectableAction {

    private final PhysicalCard _performingCard;
    private int _cardsAlreadyDrawnCount;
    private final int _minCardsToDraw;
    private final int _maxCardsToDraw;
    private Integer _cardCountLastSelected;
    private int _cardsDrawnSinceLastSelection;

    public DrawCardsAction(DefaultGame cardGame, PhysicalCard performingCard, String performingPlayerName, int min, int max) {
        super(cardGame, performingPlayerName, ActionType.DRAW_CARD);
        _performingCard = performingCard;
        _minCardsToDraw = min;
        _maxCardsToDraw = max;
    }

    public DrawCardsAction(DefaultGame cardGame, PhysicalCard performingCard, String performingPlayerName, int count) {
        super(cardGame, performingPlayerName, ActionType.DRAW_CARD);
        _performingCard = performingCard;
        _minCardsToDraw = count;
        _maxCardsToDraw = count;
    }

    @Override
    public PhysicalCard getPerformingCard() {
        return _performingCard;
    }

    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        try {
            Player performingPlayer = cardGame.getPlayer(_performingPlayerId);
            return performingPlayer.getCardsInDrawDeck().size() >= _minCardsToDraw && _maxCardsToDraw > 0;
        } catch(PlayerNotFoundException exp) {
            cardGame.sendErrorMessage(exp);
            return false;
        }
    }

    @Override
    protected void processEffect(DefaultGame cardGame) {
        if (_minCardsToDraw == 1 && _maxCardsToDraw == 1) {
            cardGame.getGameState().playerDrawsCard(_performingPlayerId);
            setAsSuccessful();
            saveResult(new ActionResult(ActionResult.Type.DRAW_CARD, _performingPlayerId, this), cardGame);
        } else if ((_cardCountLastSelected != null && _cardCountLastSelected == 0) ||
                _cardsAlreadyDrawnCount == _maxCardsToDraw) {
            if (_cardsAlreadyDrawnCount == 0) {
                setAsFailed();
            } else {
                setAsSuccessful();
            }
        } else if (_cardCountLastSelected == null || _cardsDrawnSinceLastSelection == _cardCountLastSelected) {
            AwaitingDecision selectCardCountDecision = new IntegerAwaitingDecision(_performingPlayerId,
                    DecisionContext.SELECT_NUMBER_OF_CARDS_TO_DRAW, _minCardsToDraw - _cardsAlreadyDrawnCount,
                    _maxCardsToDraw - _cardsAlreadyDrawnCount, cardGame) {
                @Override
                public void decisionMade(String result) throws DecisionResultInvalidException {
                    int selectedNumber = Integer.parseInt(result);
                    if (selectedNumber > _maxCardsToDraw - _cardsAlreadyDrawnCount) {
                        throw new DecisionResultInvalidException("Cannot draw that many cards");
                    } else {
                        _cardCountLastSelected = selectedNumber;
                        _cardsDrawnSinceLastSelection = 0;
                    }
                }
            };
            cardGame.sendAwaitingDecision(selectCardCountDecision);
        } else if (_cardsDrawnSinceLastSelection < _cardCountLastSelected) {
            _cardsAlreadyDrawnCount++;
            _cardsDrawnSinceLastSelection++;
            cardGame.addActionToStack(new DrawSingleCardAction(cardGame, _performingPlayerId));
        }
    }

}