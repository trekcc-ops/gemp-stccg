package com.gempukku.stccg.actions.blueprints;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionWithSubActions;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.player.PlayerNotFoundException;
import com.gempukku.stccg.requirement.Requirement;

import java.util.ArrayList;
import java.util.List;

public class ConditionalSubActionBlueprint implements SubActionBlueprint {

    private final SubActionBlueprint _trueAction;
    private final SubActionBlueprint _falseAction;
    private final Requirement _ifCondition;

    @JsonCreator
    private ConditionalSubActionBlueprint(
            @JsonProperty(value = "ifCondition")
            Requirement ifCondition,
            @JsonProperty(value = "actionIfTrue")
            SubActionBlueprint trueAction,
            @JsonProperty(value = "actionIfFalse")
            SubActionBlueprint falseAction) {
        _trueAction = trueAction;
        _falseAction = falseAction;
        _ifCondition = ifCondition;
    }

    @Override
    public List<Action> createActions(DefaultGame cardGame, ActionWithSubActions action, GameTextContext context)
            throws InvalidGameLogicException, InvalidCardDefinitionException, PlayerNotFoundException {

        List<Action> result = new ArrayList<>();

        boolean isConditionTrue = _ifCondition.accepts(context, cardGame);
        if (isConditionTrue) {
            result.addAll(_trueAction.createActions(cardGame, action, context));
        } else {
            result.addAll(_falseAction.createActions(cardGame, action, context));
        }
        return result;
    }

}