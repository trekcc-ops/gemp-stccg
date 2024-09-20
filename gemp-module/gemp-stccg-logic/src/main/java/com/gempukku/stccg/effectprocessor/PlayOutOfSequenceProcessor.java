package com.gempukku.stccg.effectprocessor;

import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.requirement.Requirement;
import org.json.simple.JSONObject;

public class PlayOutOfSequenceProcessor implements EffectProcessor {
    @Override
    public void processEffect(JSONObject value, CardBlueprint blueprint, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {
        environment.validateAllowedFields(value, "requires");

        final Requirement[] conditions = environment.getRequirementsFromJSON(value);

        blueprint.appendPlayOutOfSequenceCondition(actionContext -> actionContext.acceptsAllRequirements(conditions));
    }
}
