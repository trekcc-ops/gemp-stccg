package com.gempukku.stccg.modifiers;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.ModifierSource;
import com.gempukku.stccg.requirement.Requirement;
import com.gempukku.stccg.common.filterable.Phase;

public class SkipPhase implements ModifierSourceProducer {
    @Override
    public ModifierSource getModifierSource(JsonNode object, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        environment.validateAllowedFields(object, "requires", "phase");

        final Phase phase = environment.getEnum(Phase.class, object.get("phase").textValue(), "phase");
        final Requirement[] requirements = environment.getRequirementsFromJSON(object);

        return actionContext -> new ShouldSkipPhaseModifier(actionContext.getSource(),
                new RequirementCondition(requirements, actionContext), phase);
    }
}
