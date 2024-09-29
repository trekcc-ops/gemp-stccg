package com.gempukku.stccg.modifiers;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.requirement.Requirement;

public class CantDiscardCardsFromHandOrTopOfDrawDeck implements ModifierSourceProducer {
    @Override
    public ModifierSource getModifierSource(JsonNode object, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        environment.validateAllowedFields(object, "requires", "filter");

        final FilterableSource filterableSource = environment.getFilterable(object);
        final Requirement[] requirements = environment.getRequirementsFromJSON(object);

        return actionContext ->
                new CantDiscardCardsFromHandOrTopOfDeckModifier(actionContext.getSource(),
                        new RequirementCondition(requirements, actionContext),
                        actionContext.getPerformingPlayerId(),
                        filterableSource.getFilterable(actionContext));
    }
}
