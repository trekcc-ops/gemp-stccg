package com.gempukku.stccg.cards.blueprints.actionsource;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.actions.turn.OptionalTriggerAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.blueprints.trigger.TriggerChecker;
import com.gempukku.stccg.cards.blueprints.trigger.TriggerCheckerFactory;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.RequiredType;
import com.gempukku.stccg.common.filterable.TriggerTiming;

public class OptionalTriggerActionSource extends TriggerActionSource {
    private final TriggerTiming _triggerTiming;
    private final RequiredType _requiredType = RequiredType.OPTIONAL;

    public OptionalTriggerActionSource(@JsonProperty("text")
                                       String text,
                                       @JsonProperty(value="limitPerTurn", defaultValue="0")
                                       int limitPerTurn,
                                       @JsonProperty("phase")
                                       Phase phase,
                                       @JsonProperty("trigger")
                                       JsonNode triggerNode,
                                       @JsonProperty("requires")
                                       JsonNode requirementNode,
                                       @JsonProperty("cost")
                                       JsonNode costNode,
                                       @JsonProperty("effect")
                                       JsonNode effectNode) throws InvalidCardDefinitionException {
        super(text, limitPerTurn, phase);
        TriggerChecker triggerChecker = TriggerCheckerFactory.getTriggerChecker(triggerNode);
        _triggerTiming = triggerChecker.isBefore() ? TriggerTiming.BEFORE : TriggerTiming.AFTER;
        addRequirement(triggerChecker);
        processRequirementsCostsAndEffects(requirementNode, costNode, effectNode);
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