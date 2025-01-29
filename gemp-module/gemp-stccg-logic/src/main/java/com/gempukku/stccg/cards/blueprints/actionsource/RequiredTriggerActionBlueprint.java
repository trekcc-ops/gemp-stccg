package com.gempukku.stccg.cards.blueprints.actionsource;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.actions.turn.RequiredTriggerAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.DefaultActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.blueprints.effect.EffectBlueprint;
import com.gempukku.stccg.cards.blueprints.trigger.TriggerChecker;
import com.gempukku.stccg.cards.blueprints.trigger.TriggerCheckerFactory;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.TriggerTiming;
import com.gempukku.stccg.common.filterable.RequiredType;

import java.util.List;

public class RequiredTriggerActionBlueprint extends TriggerActionBlueprint {

    private final TriggerTiming _triggerTiming;
    private final RequiredType _requiredType = RequiredType.REQUIRED;

    public RequiredTriggerActionBlueprint(@JsonProperty("text")
                                       String text,
                                          @JsonProperty(value="limitPerTurn", defaultValue="0")
                                       int limitPerTurn,
                                          @JsonProperty("phase")
                                       Phase phase,
                                          @JsonProperty("trigger")
                                       JsonNode triggerNode,
                                          @JsonProperty("requires")
                                       JsonNode requirementNode,
                                          @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
                                          @JsonProperty("cost")
                                          List<EffectBlueprint> costs,
                                          @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
                                          @JsonProperty("effect")
                                       List<EffectBlueprint> effects) throws InvalidCardDefinitionException {
        super(text, limitPerTurn, phase);
        TriggerChecker triggerChecker = TriggerCheckerFactory.getTriggerChecker(triggerNode);
        _triggerTiming = triggerChecker.isBefore() ? TriggerTiming.BEFORE : TriggerTiming.AFTER;
        addRequirement(triggerChecker);
        processRequirementsCostsAndEffects(requirementNode, costs, effects);
    }

    public RequiredTriggerAction createAction(PhysicalCard card) {
        return new RequiredTriggerAction(card);
    }

    @Override
    protected RequiredTriggerAction createActionAndAppendToContext(PhysicalCard card, ActionContext actionContext) {
        if (isValid(actionContext)) {
            RequiredTriggerAction action = createAction(card);
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


    public TriggerTiming getTiming() { return _triggerTiming; }
    public RequiredType getRequiredType() { return _requiredType; }

}