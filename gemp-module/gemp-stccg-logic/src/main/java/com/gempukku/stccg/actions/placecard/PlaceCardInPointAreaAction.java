package com.gempukku.stccg.actions.placecard;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.actions.scorepoints.ScorePointsAction;
import com.gempukku.stccg.actions.targetresolver.ActionCardResolver;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.evaluator.SingleValueSource;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.gamestate.GameState;

import java.util.Collection;

public class PlaceCardInPointAreaAction extends ActionyAction {

    @JsonIdentityReference(alwaysAsId=true)
    @JsonProperty("cardTarget")
    private final ActionCardResolver _cardTarget;

    private final SingleValueSource _pointValue;
    private final PhysicalCard _performingCard;

    public PlaceCardInPointAreaAction(DefaultGame cardGame, String performingPlayerName, ActionCardResolver cardTarget,
                                      SingleValueSource pointValue, GameTextContext context, PhysicalCard performingCard) {
        super(cardGame, performingPlayerName, ActionType.PLACE_CARD_IN_POINT_AREA, context);
        _cardTarget = cardTarget;
        _cardTargets.add(_cardTarget);
        _pointValue = pointValue;
        _performingCard = performingCard;
    }


    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        return true;
    }

    @Override
    public void processEffect(DefaultGame cardGame) {
        Collection<PhysicalCard> cardBeingPlaced = getTargetCards();
        GameState gameState = cardGame.getGameState();
        gameState.removeCardsFromZoneWithoutSendingToClient(cardGame, cardBeingPlaced);

        for (PhysicalCard card : cardBeingPlaced) {
            gameState.addCardToZone(cardGame, card, Zone.POINT_AREA, _actionContext);
        }

        int pointsToScore = _pointValue.evaluateExpression(cardGame, _actionContext);
        cardGame.addActionToStack(new ScorePointsAction(cardGame, _performingCard, _performingPlayerId, pointsToScore, _actionContext));
        setAsSuccessful();
    }

    @SuppressWarnings("unused")
    @JsonProperty("targetCardIds")
    private Collection<PhysicalCard> getTargetCards() {
        return _cardTarget.getCards();
    }
}