package com.gempukku.stccg.actions.blueprints;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionWithSubActions;
import com.gempukku.stccg.actions.draw.DrawCardsAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.evaluator.ConstantValueSource;
import com.gempukku.stccg.evaluator.SingleValueSource;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;
import com.gempukku.stccg.player.PlayerResolver;
import com.gempukku.stccg.player.PlayerSource;

import java.util.List;
import java.util.Objects;

public class DrawCardsActionBlueprint implements SubActionBlueprint {

    private final SingleValueSource _countSource;
    private final PlayerSource _drawingPlayerSource;

    public DrawCardsActionBlueprint(@JsonProperty(value = "count")
                                    SingleValueSource count,
                                    @JsonProperty(value = "player")
                                    String playerText) throws InvalidCardDefinitionException {
        _drawingPlayerSource = PlayerResolver.resolvePlayer(Objects.requireNonNullElse(playerText, "you"));
        _countSource = Objects.requireNonNullElse(count, new ConstantValueSource(1));
    }

    @Override
    public List<Action> createActions(DefaultGame cardGame, ActionWithSubActions action, ActionContext context)
            throws InvalidGameLogicException, InvalidCardDefinitionException, PlayerNotFoundException {
        final String targetPlayerId;
        targetPlayerId = context.getPerformingPlayerId();
        final int count = _countSource.evaluateExpression(cardGame, context);
        return List.of(new DrawCardsAction(cardGame, context.card(), targetPlayerId, count));
    }

    @Override
    public boolean isPlayableInFull(DefaultGame cardGame, ActionContext context) {
        try {
            final int count = _countSource.evaluateExpression(cardGame, context);
            final String targetPlayerId = _drawingPlayerSource.getPlayerName(cardGame, context);
            Player targetPlayer = cardGame.getPlayer(targetPlayerId);
            return targetPlayer.getCardsInDrawDeck().size() >= count;
        } catch(PlayerNotFoundException exp) {
            cardGame.sendErrorMessage(exp);
            return false;
        }
    }
}