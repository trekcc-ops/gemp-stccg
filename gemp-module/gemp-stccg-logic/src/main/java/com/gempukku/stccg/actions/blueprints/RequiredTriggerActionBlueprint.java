package com.gempukku.stccg.actions.blueprints;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.actions.turn.RequiredTriggerAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.DefaultActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.requirement.Requirement;
import com.gempukku.stccg.requirement.trigger.TriggerChecker;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;

import java.util.List;

public class RequiredTriggerActionBlueprint extends TriggerActionBlueprint {

    public RequiredTriggerActionBlueprint(@JsonProperty("text")
                                       String text,
                                          @JsonProperty(value="limitPerTurn", defaultValue="0")
                                       int limitPerTurn,
                                          @JsonProperty(value="triggerDuringSeed", required = true)
                                      boolean triggerDuringSeed,
                                          @JsonProperty("trigger")
                                       TriggerChecker triggerChecker,
                                          @JsonProperty("requires")
                                       List<Requirement> requirements,
                                          @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
                                          @JsonProperty("cost")
                                          List<SubActionBlueprint> costs,
                                          @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
                                          @JsonProperty("effect")
                                       List<SubActionBlueprint> effects) throws InvalidCardDefinitionException {
        super(text, limitPerTurn, triggerChecker, requirements, costs, effects, triggerDuringSeed);
    }

    @Override
    protected RequiredTriggerAction createActionAndAppendToContext(PhysicalCard card, ActionContext actionContext) {
        if (isValid(actionContext)) {
            RequiredTriggerAction action = new RequiredTriggerAction(card);
            appendActionToContext(action, actionContext);
            return action;
        }
        return null;
    }

    @Override
    public RequiredTriggerAction createActionWithNewContext(PhysicalCard card, ActionResult actionResult) {
        return createActionAndAppendToContext(card,
                new DefaultActionContext(card.getOwnerName(), card, actionResult));
    }

}