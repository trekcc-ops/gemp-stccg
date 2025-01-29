package com.gempukku.stccg.cards.blueprints.actionsource;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.actions.turn.ActivateCardAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Phase;

public class ActivateCardActionSource extends DefaultActionSource {

    public ActivateCardActionSource(@JsonProperty("text")
                                    String text,
                                    @JsonProperty(value="limitPerTurn", defaultValue="0")
                                    int limitPerTurn,
                                    @JsonProperty("phase")
                                    Phase phase,
                                    @JsonProperty("requires")
                                    JsonNode requirementNode,
                                    @JsonProperty("cost")
                                    JsonNode costNode,
                                    @JsonProperty("effect")
                                    JsonNode effectNode) throws InvalidCardDefinitionException {
            super(text, limitPerTurn, phase);
            processRequirementsCostsAndEffects(requirementNode, costNode, effectNode);
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