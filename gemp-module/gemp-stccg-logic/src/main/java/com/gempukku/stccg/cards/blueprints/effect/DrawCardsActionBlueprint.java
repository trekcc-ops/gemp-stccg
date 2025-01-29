package com.gempukku.stccg.cards.blueprints.effect;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.CardPerformedAction;
import com.gempukku.stccg.actions.draw.DrawCardsAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.ConstantValueSource;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.PlayerSource;
import com.gempukku.stccg.cards.blueprints.BlueprintUtils;
import com.gempukku.stccg.cards.blueprints.ValueSource;
import com.gempukku.stccg.cards.blueprints.resolver.PlayerResolver;
import com.gempukku.stccg.cards.blueprints.resolver.ValueResolver;
import com.gempukku.stccg.evaluator.ConstantEvaluator;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.PlayerNotFoundException;

import java.util.LinkedList;
import java.util.List;

public class DrawCardsActionBlueprint extends DelayedEffectBlueprint {

    private final ValueSource _countSource;
    private final PlayerSource _drawingPlayerSource;

    DrawCardsActionBlueprint(@JsonProperty(value = "count")
                             String count,
                             @JsonProperty(value = "player")
                             String playerText) throws InvalidCardDefinitionException {
        _drawingPlayerSource = (playerText == null) ?
                ActionContext::getPerformingPlayerId : PlayerResolver.resolvePlayer(playerText);
        _countSource = (count == null) ? new ConstantEvaluator(1) : ValueResolver.resolveEvaluator(count);
    }

    @Override
    protected List<Action> createActions(CardPerformedAction action, ActionContext context)
            throws InvalidGameLogicException, InvalidCardDefinitionException, PlayerNotFoundException {
        final String targetPlayerId = _drawingPlayerSource.getPlayerId(context);
        DefaultGame cardGame = context.getGame();
        Player targetPlayer = cardGame.getPlayer(targetPlayerId);
        final int count = _countSource.evaluateExpression(context, null);
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
            final int count = _countSource.evaluateExpression(context, null);
            final String targetPlayerId = _drawingPlayerSource.getPlayerId(context);
            Player targetPlayer = context.getGame().getPlayer(targetPlayerId);
            return targetPlayer.getCardsInDrawDeck().size() >= count;
        } catch(PlayerNotFoundException exp) {
            context.getGame().sendErrorMessage(exp);
            return false;
        }
    }
}