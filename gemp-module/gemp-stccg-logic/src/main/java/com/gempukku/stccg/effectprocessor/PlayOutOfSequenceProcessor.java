package com.gempukku.stccg.effectprocessor;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.requirement.Requirement;

public class PlayOutOfSequenceProcessor implements EffectProcessor {
    @Override
    public void processEffect(JsonNode node, CardBlueprint blueprint, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {
        environment.validateAllowedFields(node, "requires");

        final Requirement[] conditions = environment.getRequirementsFromJSON(node);

        blueprint.appendPlayOutOfSequenceCondition(actionContext -> actionContext.acceptsAllRequirements(conditions));
    }
}
