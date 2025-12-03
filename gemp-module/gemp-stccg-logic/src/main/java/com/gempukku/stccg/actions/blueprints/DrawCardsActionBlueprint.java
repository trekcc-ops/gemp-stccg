package com.gempukku.stccg.actions.blueprints;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.CardPerformedAction;
import com.gempukku.stccg.actions.draw.DrawCardsAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.player.*;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.evaluator.ConstantEvaluator;
import com.gempukku.stccg.evaluator.ValueSource;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;

import java.util.List;
import java.util.Objects;

public class DrawCardsActionBlueprint implements SubActionBlueprint {

    private final ValueSource _countSource;
    private final PlayerSource _drawingPlayerSource;

    public DrawCardsActionBlueprint(@JsonProperty(value = "count")
                                    ValueSource count,
                                    @JsonProperty(value = "player")
                                    String playerText) throws InvalidCardDefinitionException {
        _drawingPlayerSource = (playerText == null) ?
                new YouPlayerSource() : PlayerResolver.resolvePlayer(playerText);
        _countSource = Objects.requireNonNullElse(count, new ConstantEvaluator(1));
    }

    @Override
    public List<Action> createActions(DefaultGame cardGame, CardPerformedAction action, ActionContext context)
            throws InvalidGameLogicException, InvalidCardDefinitionException, PlayerNotFoundException {
        final String targetPlayerId = _drawingPlayerSource.getPlayerId(cardGame, context);
        PhysicalCard performingCard = context.getPerformingCard(cardGame);
        final int count = (int) _countSource.evaluateExpression(cardGame, context);
        return List.of(new DrawCardsAction(performingCard, targetPlayerId, count, cardGame));
    }

    @Override
    public boolean isPlayableInFull(DefaultGame cardGame, ActionContext context) {
        try {
            final int count = (int) _countSource.evaluateExpression(cardGame, context);
            final String targetPlayerId = _drawingPlayerSource.getPlayerId(cardGame, context);
            Player targetPlayer = cardGame.getPlayer(targetPlayerId);
            return targetPlayer.getCardsInDrawDeck().size() >= count;
        } catch(PlayerNotFoundException | InvalidGameLogicException exp) {
            cardGame.sendErrorMessage(exp);
            return false;
        }
    }
}