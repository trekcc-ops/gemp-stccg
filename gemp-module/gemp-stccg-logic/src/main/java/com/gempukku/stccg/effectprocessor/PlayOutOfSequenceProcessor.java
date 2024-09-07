package com.gempukku.stccg.effectprocessor;

import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.requirement.Requirement;
import com.gempukku.stccg.requirement.RequirementUtils;
import org.json.simple.JSONObject;

public class PlayOutOfSequenceProcessor implements EffectProcessor {
    @Override
    public void processEffect(JSONObject value, CardBlueprint blueprint, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {
        environment.validateAllowedFields(value, "requires");

        final Requirement[] conditions = environment.getRequirementsFromJSON(value);

        blueprint.appendPlayOutOfSequenceCondition(
                actionContext -> RequirementUtils.acceptsAllRequirements(conditions, actionContext)
        );
    }
}
