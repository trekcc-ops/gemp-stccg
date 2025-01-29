package com.gempukku.stccg.cards.blueprints.actionsource;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.actions.turn.ActivateCardAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.blueprints.effect.SubActionBlueprint;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.requirement.Requirement;

import java.util.List;

public class ActivateCardActionBlueprint extends DefaultActionBlueprint {

    public ActivateCardActionBlueprint(@JsonProperty("text")
                                    String text,
                                       @JsonProperty(value="limitPerTurn", defaultValue="0")
                                    int limitPerTurn,
                                       @JsonProperty("phase")
                                    Phase phase,
                                       @JsonProperty("requires")
                                       @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
                                    List<Requirement> requirements,
                                       @JsonProperty("cost")
                                       @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
                                       List<SubActionBlueprint> costs,
                                       @JsonProperty("effect")
                                       @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
                                    List<SubActionBlueprint> effects) throws InvalidCardDefinitionException {
            super(text, limitPerTurn, phase);
            processRequirementsCostsAndEffects(requirements, costs, effects);
    }

    public ActivateCardAction createAction(PhysicalCard card) { return new ActivateCardAction(card.getGame(), card); }

    @Override
    protected ActivateCardAction createActionAndAppendToContext(PhysicalCard card, ActionContext actionContext) {
        if (isValid(actionContext)) {
            ActivateCardAction action = createAction(card);
            appendActionToContext(action, actionContext);
            return action;
        }
        return null;
    }

}