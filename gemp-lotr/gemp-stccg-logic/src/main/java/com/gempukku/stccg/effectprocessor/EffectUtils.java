package com.gempukku.stccg.effectprocessor;

import com.gempukku.stccg.actions.DefaultActionSource;
import com.gempukku.stccg.cards.CardGenerationEnvironment;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.requirement.Requirement;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
import com.gempukku.stccg.effectappender.EffectAppender;
import com.gempukku.stccg.effectappender.EffectAppenderFactory;
import org.json.simple.JSONObject;

public class EffectUtils {
    public static void processRequirementsCostsAndEffects(JSONObject value, CardGenerationEnvironment environment, DefaultActionSource actionSource) throws InvalidCardDefinitionException {
        final JSONObject[] requirementArray = FieldUtils.getObjectArray(value.get("requires"), "requires");
        for (JSONObject requirement : requirementArray) {
            final Requirement conditionRequirement = environment.getRequirementFactory().getRequirement(requirement, environment);
            actionSource.addPlayRequirement(conditionRequirement);
        }

        processCostsAndEffects(value, environment, actionSource);
    }

    public static void processCostsAndEffects(JSONObject value, CardGenerationEnvironment environment, DefaultActionSource actionSource) throws InvalidCardDefinitionException {
        final JSONObject[] costArray = FieldUtils.getObjectArray(value.get("cost"), "cost");
        final JSONObject[] effectArray = FieldUtils.getObjectArray(value.get("effect"), "effect");

        if (costArray.length == 0 && effectArray.length == 0)
            throw new InvalidCardDefinitionException("Action does not contain a cost, nor effect");

        final EffectAppenderFactory effectAppenderFactory = environment.getEffectAppenderFactory();
        for (JSONObject cost : costArray) {
            final EffectAppender effectAppender = effectAppenderFactory.getEffectAppender(cost, environment);
            actionSource.addPlayRequirement(
                    effectAppender::isPlayableInFull);
            actionSource.addCost(effectAppender);
        }

        for (JSONObject effect : effectArray) {
            final EffectAppender effectAppender = effectAppenderFactory.getEffectAppender(effect, environment);
            if (effectAppender.isPlayabilityCheckedForEffect())
                actionSource.addPlayRequirement(
                        effectAppender::isPlayableInFull);
            actionSource.addEffect(effectAppender);
        }
    }

}
