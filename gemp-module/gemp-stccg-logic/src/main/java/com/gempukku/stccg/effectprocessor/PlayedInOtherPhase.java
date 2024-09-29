package com.gempukku.stccg.effectprocessor;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.requirement.Requirement;

public class PlayedInOtherPhase implements EffectProcessor {
    @Override
    public void processEffect(JsonNode node, CardBlueprint blueprint, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {
        environment.validateAllowedFields(node, "phase", "requires");

        final Phase phase = environment.getEnum(Phase.class, node.get("phase").textValue(), "phase");

        final Requirement[] conditions = environment.getRequirementsFromJSON(node);

        blueprint.appendPlayInOtherPhaseCondition(
                actionContext -> actionContext.getGameState().getCurrentPhase() == phase &&
                        actionContext.acceptsAllRequirements(conditions)
        );
    }
}
