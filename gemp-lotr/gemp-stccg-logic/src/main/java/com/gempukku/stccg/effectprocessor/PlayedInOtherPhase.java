package com.gempukku.stccg.effectprocessor;

import com.gempukku.stccg.cards.BuiltCardBlueprint;
import com.gempukku.stccg.cards.CardGenerationEnvironment;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
import com.gempukku.stccg.requirement.Requirement;
import com.gempukku.stccg.requirement.RequirementUtils;
import org.json.simple.JSONObject;

public class PlayedInOtherPhase implements EffectProcessor {
    @Override
    public void processEffect(JSONObject value, BuiltCardBlueprint blueprint, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(value, "phase", "requires");

        final Phase phase = FieldUtils.getEnum(Phase.class, value.get("phase"), "phase");
        final JSONObject[] conditionArray = FieldUtils.getObjectArray(value.get("requires"), "requires");

        final Requirement[] conditions = environment.getRequirementFactory().getRequirements(conditionArray, environment);

        blueprint.appendPlayInOtherPhaseCondition(
                actionContext -> {
                    if (actionContext.getGameState().getCurrentPhase() != phase)
                        return false;

                    return RequirementUtils.acceptsAllRequirements(conditions, actionContext);
                }
        );
    }
}
