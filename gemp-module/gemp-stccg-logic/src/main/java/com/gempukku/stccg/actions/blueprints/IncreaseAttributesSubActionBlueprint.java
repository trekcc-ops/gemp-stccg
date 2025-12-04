package com.gempukku.stccg.actions.blueprints;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionCardResolver;
import com.gempukku.stccg.actions.CardPerformedAction;
import com.gempukku.stccg.actions.CardTargetBlueprint;
import com.gempukku.stccg.actions.modifiers.IncreaseAttributesUntilEndOfTurnAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.CardAttribute;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class IncreaseAttributesSubActionBlueprint implements SubActionBlueprint {

    private final boolean _untilEndOfTurn;
    private final CardTargetBlueprint _cardTarget;
    private final int _amount;
    private final List<CardAttribute> _attributes;

    public IncreaseAttributesSubActionBlueprint(
                                        @JsonProperty("attributes")
                                        List<CardAttribute> attributes,
                                      @JsonProperty("until")
                                      String untilString,
                                      @JsonProperty("cardModified")
                                      CardTargetBlueprint cardTarget,
                                      @JsonProperty(value = "amount", required = true)
                                                   int amount
                                      ) throws InvalidCardDefinitionException {
        if (Objects.equals(untilString, "endOfThisTurn")) {
            _untilEndOfTurn = true;
        } else {
            throw new InvalidCardDefinitionException("Card blueprint library is not equipped to handle until phrases " +
                    "except for 'end of turn'");
        }
        _cardTarget = cardTarget;
        _amount = amount;
        _attributes = attributes;
    }

    @Override
    public List<Action> createActions(DefaultGame cardGame, CardPerformedAction parentAction, ActionContext context)
            throws InvalidGameLogicException {
        List<Action> result = new ArrayList<>();
        PhysicalCard performingCard = context.getPerformingCard(cardGame);
        for (String playerName : cardGame.getAllPlayerIds()) {
            if (performingCard.isControlledBy(playerName)) {
                ActionCardResolver cardTarget = _cardTarget.getTargetResolver(cardGame, context);
                if (_untilEndOfTurn) {
                    Action action = new IncreaseAttributesUntilEndOfTurnAction(_attributes, cardGame, performingCard,
                            context.getPerformingPlayerId(), cardTarget, _amount);
                    result.add(action);
                }
            }
        }
        return result;
    }

}