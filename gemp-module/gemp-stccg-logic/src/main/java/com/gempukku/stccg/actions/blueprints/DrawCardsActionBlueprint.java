package com.gempukku.stccg.actions.blueprints;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionWithSubActions;
import com.gempukku.stccg.actions.choose.MakeDecisionAction;
import com.gempukku.stccg.actions.draw.DrawCardsAction;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.decisions.AwaitingDecision;
import com.gempukku.stccg.decisions.YesNoDecision;
import com.gempukku.stccg.evaluator.ConstantValueSource;
import com.gempukku.stccg.evaluator.ValueSource;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;
import com.gempukku.stccg.player.PlayerResolver;
import com.gempukku.stccg.player.PlayerSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DrawCardsActionBlueprint implements SubActionBlueprint {

    private final ValueSource _countSource;
    private final PlayerSource _drawingPlayerSource;
    private final boolean _optional;

    public DrawCardsActionBlueprint(@JsonProperty(value = "count")
                                    ValueSource count,
                                    @JsonProperty(value = "player")
                                    String playerText,
                                    @JsonProperty(value = "optional") boolean optional) throws InvalidCardDefinitionException {
        _drawingPlayerSource = PlayerResolver.resolvePlayer(Objects.requireNonNullElse(playerText, "you"));
        _countSource = Objects.requireNonNullElse(count, new ConstantValueSource(1));
        _optional = optional;
    }

    @Override
    public List<Action> createActions(DefaultGame cardGame, ActionWithSubActions action, GameTextContext context)
            throws InvalidGameLogicException, InvalidCardDefinitionException, PlayerNotFoundException {
        final String targetPlayerId;
        targetPlayerId = _drawingPlayerSource.getPlayerName(cardGame, context);
        int min = Math.max(_countSource.getMinimum(cardGame, context), 0);
        int max = Math.min(_countSource.getMaximum(cardGame, context),
                cardGame.getPlayer(targetPlayerId).getCardsInDrawDeck().size());

        List<Action> result = new ArrayList<>();

        if (_optional) {
            Action decisionAction = new MakeDecisionAction(cardGame, targetPlayerId, "", context) {

                @Override
                protected AwaitingDecision getDecision(DefaultGame cardGame) {
                    AwaitingDecision decisionToSend = new YesNoDecision(targetPlayerId, "Do you want to draw?", cardGame) {
                        @Override
                        protected void yes() {
                            cardGame.addActionToStack(makeAction(cardGame, context.card(), targetPlayerId, min, max));
                        }

                        @Override
                        protected void no() {

                        }
                    };
                    return decisionToSend;
                }
            };
            result.add(decisionAction);
        } else {
            result.add(makeAction(cardGame, context.card(), targetPlayerId, min, max));
        }
        return result;
    }

    private Action makeAction(DefaultGame cardGame, PhysicalCard thisCard, String performingPlayerName,
                              int min, int max) {
        return new DrawCardsAction(cardGame, thisCard, performingPlayerName, min, max);
    }

    @Override
    public boolean isPlayableInFull(DefaultGame cardGame, GameTextContext context) {
        try {
            final int min = Math.max(_countSource.getMinimum(cardGame, context), 0);
            final String targetPlayerId = _drawingPlayerSource.getPlayerName(cardGame, context);
            Player targetPlayer = cardGame.getPlayer(targetPlayerId);
            int max = _countSource.getMaximum(cardGame, context);
            return targetPlayer.getCardsInDrawDeck().size() >= min && max >= min && max > 0;
        } catch(PlayerNotFoundException exp) {
            cardGame.sendErrorMessage(exp);
            return false;
        }
    }
}