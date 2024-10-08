package com.gempukku.stccg.cards.blueprints.modifiersourceproducer;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.blueprints.BlueprintUtils;
import com.gempukku.stccg.cards.blueprints.FilterableSource;
import com.gempukku.stccg.cards.blueprints.requirement.Requirement;
import com.gempukku.stccg.cards.blueprints.requirement.RequirementFactory;
import com.gempukku.stccg.modifiers.CantDiscardCardsFromHandOrTopOfDeckModifier;
import com.gempukku.stccg.modifiers.RequirementCondition;

public class CantDiscardCardsFromHandOrTopOfDrawDeck implements ModifierSourceProducer {
    @Override
    public ModifierSource getModifierSource(JsonNode object) throws InvalidCardDefinitionException {
        BlueprintUtils.validateAllowedFields(object, "requires", "filter");

        final FilterableSource filterableSource = BlueprintUtils.getFilterable(object);
        final Requirement[] requirements = RequirementFactory.getRequirements(object);

        return actionContext ->
                new CantDiscardCardsFromHandOrTopOfDeckModifier(actionContext.getSource(),
                        new RequirementCondition(requirements, actionContext),
                        actionContext.getPerformingPlayerId(),
                        filterableSource.getFilterable(actionContext));
    }
}