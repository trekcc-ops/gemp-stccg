package com.gempukku.stccg.cards.blueprints.actionsource;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.actions.turn.OptionalTriggerAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.blueprints.effect.SubActionBlueprint;
import com.gempukku.stccg.requirement.Requirement;
import com.gempukku.stccg.requirement.trigger.TriggerChecker;
import com.gempukku.stccg.requirement.trigger.TriggerCheckerFactory;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.RequiredType;
import com.gempukku.stccg.common.filterable.TriggerTiming;

import java.util.List;

public class OptionalTriggerActionBlueprint extends TriggerActionBlueprint {
    private final TriggerTiming _triggerTiming;
    private final RequiredType _requiredType = RequiredType.OPTIONAL;

    public OptionalTriggerActionBlueprint(@JsonProperty("text")
                                       String text,
                                          @JsonProperty(value="limitPerTurn", defaultValue="0")
                                       int limitPerTurn,
                                          @JsonProperty("phase")
                                       Phase phase,
                                          @JsonProperty("trigger")
                                       JsonNode triggerNode,
                                          @JsonProperty("requires")
                                       List<Requirement> requirements,
                                          @JsonProperty("cost")
                                          @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
                                       List<SubActionBlueprint> costs,
                                          @JsonProperty("effect")
                                          @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
                                          List<SubActionBlueprint> effects) throws InvalidCardDefinitionException {
        super(text, limitPerTurn, phase);
        TriggerChecker triggerChecker = TriggerCheckerFactory.getTriggerChecker(triggerNode);
        _triggerTiming = triggerChecker.isBefore() ? TriggerTiming.BEFORE : TriggerTiming.AFTER;
        addRequirement(triggerChecker);
        processRequirementsCostsAndEffects(requirements, costs, effects);
    }

    public OptionalTriggerAction createAction(PhysicalCard card) {
        return new OptionalTriggerAction(card, this);
    }

    @Override
    protected OptionalTriggerAction createActionAndAppendToContext(PhysicalCard card, ActionContext actionContext) {
        if (isValid(actionContext)) {
                OptionalTriggerAction action = createAction(card);
                appendActionToContext(action, actionContext);
                return action;
        }
        return null;
    }

    public TriggerTiming getTiming() { return _triggerTiming; }
    public RequiredType getRequiredType() { return _requiredType; }

}