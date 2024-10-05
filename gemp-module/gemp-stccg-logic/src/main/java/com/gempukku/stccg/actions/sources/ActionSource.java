package com.gempukku.stccg.actions.sources;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;

public interface ActionSource {

    Action createActionAndAppendToContext(PhysicalCard card, ActionContext actionContext);

    void processRequirementsCostsAndEffects(JsonNode node, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException;
}
