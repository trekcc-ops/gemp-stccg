package com.gempukku.stccg.modifiers;

import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.ModifierSource;
import com.gempukku.stccg.requirement.Requirement;
import com.gempukku.stccg.common.filterable.Phase;
import org.json.simple.JSONObject;

public class SkipPhase implements ModifierSourceProducer {
    @Override
    public ModifierSource getModifierSource(JSONObject object, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        environment.validateAllowedFields(object, "requires", "phase");

        final Phase phase = environment.getEnum(Phase.class, object.get("phase"), "phase");
        final Requirement[] requirements = environment.getRequirementsFromJSON(object);

        return actionContext -> new ShouldSkipPhaseModifier(actionContext.getSource(),
                new RequirementCondition(requirements, actionContext), phase);
    }
}
