package com.gempukku.stccg.actions.blueprints;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.choose.MakeDecisionAction;
import com.gempukku.stccg.actions.draw.DrawCardsAction;
import com.gempukku.stccg.cards.DilemmaEncounterGameTextContext;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.decisions.AwaitingDecision;
import com.gempukku.stccg.decisions.YesNoDecision;
import com.gempukku.stccg.evaluator.ConstantValueSource;
import com.gempukku.stccg.evaluator.ValueSource;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.player.*;

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

    public Action createAction(DefaultGame cardGame, GameTextContext context) {
        try {
            final String targetPlayerId;
            targetPlayerId = _drawingPlayerSource.getPlayerName(cardGame, context);
            int max = Math.min(_countSource.getMaximum(cardGame, context),
                    cardGame.getPlayer(targetPlayerId).getCardsInDrawDeck().size());
            int minFromSource = Math.max(_countSource.getMinimum(cardGame, context), 0);
            int min = (context instanceof DilemmaEncounterGameTextContext &&
                    _drawingPlayerSource instanceof YourOpponentPlayerSource) ?
                    Math.min(max, minFromSource) : minFromSource;

            if (max == 0 || max < min) {
                return null;
            }

            if (_optional) {
                return new MakeDecisionAction(cardGame, targetPlayerId, "", context) {

                    @Override
                    protected AwaitingDecision getDecision(DefaultGame cardGame1) {
                        return new YesNoDecision(targetPlayerId, "Do you want to draw?", cardGame1) {
                            @Override
                            protected void yes() {
                                cardGame1.addActionToStack(
                                        makeAction(cardGame1, context.card(), targetPlayerId, min, max));
                            }

                            @Override
                            protected void no() {

                            }
                        };
                    }
                };
            } else {
                return makeAction(cardGame, context.card(), targetPlayerId, min, max);
            }
        } catch(PlayerNotFoundException exp) {
            cardGame.sendErrorMessage(exp);
            return null;
        }
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