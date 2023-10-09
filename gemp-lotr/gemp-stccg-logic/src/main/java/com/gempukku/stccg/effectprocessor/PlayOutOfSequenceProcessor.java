package com.gempukku.stccg.effectprocessor;

import com.gempukku.stccg.cards.BuiltLotroCardBlueprint;
import com.gempukku.stccg.cards.CardGenerationEnvironment;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
import com.gempukku.stccg.game.TribblesGame;
import com.gempukku.stccg.requirement.Requirement;
import com.gempukku.stccg.requirement.RequirementUtils;
import org.json.simple.JSONObject;

public class PlayOutOfSequenceProcessor implements EffectProcessor {
    @Override
    public void processEffect(JSONObject value, BuiltLotroCardBlueprint blueprint, CardGenerationEnvironment environment)
            throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(value, "requires");

        final JSONObject[] conditionArray = FieldUtils.getObjectArray(value.get("requires"), "requires");
        final Requirement<TribblesGame>[] conditions =
                environment.getRequirementFactory().getRequirements(conditionArray, environment);

        blueprint.appendPlayOutOfSequenceCondition(
                actionContext -> RequirementUtils.acceptsAllRequirements(conditions, actionContext)
        );
    }
}
