package com.gempukku.stccg.actions.blueprints;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.actions.turn.OptionalTriggerAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.requirement.Requirement;
import com.gempukku.stccg.requirement.trigger.TriggerChecker;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;

import java.util.List;

public class OptionalTriggerActionBlueprint extends TriggerActionBlueprint {

    public OptionalTriggerActionBlueprint(@JsonProperty(value="limitPerTurn", defaultValue="0")
                                       int limitPerTurn,
                                          @JsonProperty(value="triggerDuringSeed", required = true)
                                      boolean triggerDuringSeed,
                                          @JsonProperty("trigger")
                                       TriggerChecker triggerChecker,
                                          @JsonProperty("requires")
                                       List<Requirement> requirements,
                                          @JsonProperty("cost")
                                          @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
                                       List<SubActionBlueprint> costs,
                                          @JsonProperty("effect")
                                          @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
                                          List<SubActionBlueprint> effects) throws InvalidCardDefinitionException {
        super(limitPerTurn, triggerChecker, requirements, costs, effects, triggerDuringSeed);
    }

    @Override
    public TopLevelSelectableAction createAction(DefaultGame cardGame, String performingPlayerName,
                                                 PhysicalCard thisCard, ActionResult result) {
        ActionContext actionContext = new ActionContext(performingPlayerName, thisCard, result.getAction());
        if (isValid(cardGame, actionContext)) {
            OptionalTriggerAction action = new OptionalTriggerAction(cardGame, thisCard, this, actionContext);
            appendActionToContext(cardGame, action, actionContext);
            return action;
        }
        return null;
    }
}