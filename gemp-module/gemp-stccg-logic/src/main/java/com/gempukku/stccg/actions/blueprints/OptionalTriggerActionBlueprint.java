package com.gempukku.stccg.actions.blueprints;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.turn.OptionalTriggerAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.requirement.Requirement;
import com.gempukku.stccg.requirement.trigger.TriggerChecker;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.RequiredType;

import java.util.List;

public class OptionalTriggerActionBlueprint extends TriggerActionBlueprint {
    private final RequiredType _requiredType = RequiredType.OPTIONAL;

    public OptionalTriggerActionBlueprint(@JsonProperty("text")
                                       String text,
                                          @JsonProperty(value="limitPerTurn", defaultValue="0")
                                       int limitPerTurn,
                                          @JsonProperty("phase")
                                       Phase phase,
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
        super(RequiredType.OPTIONAL, text, limitPerTurn, phase, triggerChecker, requirements, costs, effects);
    }

    @Override
    protected OptionalTriggerAction createActionAndAppendToContext(PhysicalCard card, ActionContext actionContext) {
        if (isValid(actionContext)) {
                OptionalTriggerAction action = new OptionalTriggerAction(card, this);
                appendActionToContext(action, actionContext);
                return action;
        }
        return null;
    }

    public RequiredType getRequiredType() { return _requiredType; }

}