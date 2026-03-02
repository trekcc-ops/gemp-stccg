package com.gempukku.stccg.actions.blueprints;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionWithSubActions;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.requirement.Requirement;

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

    public Action createAction(DefaultGame cardGame, ActionWithSubActions parentAction, GameTextContext context) {
        boolean isConditionTrue = _ifCondition.accepts(context, cardGame);
        if (isConditionTrue) {
            return _trueAction.createAction(cardGame, parentAction, context);
        } else {
            return _falseAction.createAction(cardGame, parentAction, context);
        }
    }

}