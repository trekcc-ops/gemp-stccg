package com.gempukku.stccg.actions.blueprints;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.CardPerformedAction;
import com.gempukku.stccg.actions.draw.DrawCardsAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.PlayerSource;
import com.gempukku.stccg.player.PlayerResolver;
import com.gempukku.stccg.evaluator.ConstantEvaluator;
import com.gempukku.stccg.evaluator.ValueSource;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class DrawCardsActionBlueprint implements SubActionBlueprint {

    private final ValueSource _countSource;
    private final PlayerSource _drawingPlayerSource;

    DrawCardsActionBlueprint(@JsonProperty(value = "count")
                             ValueSource count,
                             @JsonProperty(value = "player")
                             String playerText) throws InvalidCardDefinitionException {
        _drawingPlayerSource = (playerText == null) ?
                ActionContext::getPerformingPlayerId : PlayerResolver.resolvePlayer(playerText);
        _countSource = Objects.requireNonNullElse(count, new ConstantEvaluator(1));
    }

    @Override
    public List<Action> createActions(CardPerformedAction action, ActionContext context)
            throws InvalidGameLogicException, InvalidCardDefinitionException, PlayerNotFoundException {
        final String targetPlayerId = _drawingPlayerSource.getPlayerId(context);
        DefaultGame cardGame = context.getGame();
        Player targetPlayer = cardGame.getPlayer(targetPlayerId);
        final int count = _countSource.evaluateExpression(context);
        List<Action> result = new LinkedList<>();
        int numberOfEffects = 1;
        for (int i = 0; i < numberOfEffects; i++) {
            result.add(new DrawCardsAction(context.getSource(), targetPlayer, count));
        }
        return result;
    }

    @Override
    public boolean isPlayableInFull(ActionContext context) {
        try {
            final int count = _countSource.evaluateExpression(context);
            final String targetPlayerId = _drawingPlayerSource.getPlayerId(context);
            Player targetPlayer = context.getGame().getPlayer(targetPlayerId);
            return targetPlayer.getCardsInDrawDeck().size() >= count;
        } catch(PlayerNotFoundException exp) {
            context.getGame().sendErrorMessage(exp);
            return false;
        }
    }
}