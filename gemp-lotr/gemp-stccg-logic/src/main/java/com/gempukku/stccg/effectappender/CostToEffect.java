package com.gempukku.stccg.effectappender;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.SubAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.CardGenerationEnvironment;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.effects.Effect;
import com.gempukku.stccg.effects.StackActionEffect;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
import com.gempukku.stccg.requirement.Requirement;
import com.gempukku.stccg.requirement.RequirementUtils;
import org.json.simple.JSONObject;

public class CostToEffect implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "cost", "effect", "requires");

        final JSONObject[] costArray = FieldUtils.getObjectArray(effectObject.get("cost"), "cost");
        final JSONObject[] effectArray = FieldUtils.getObjectArray(effectObject.get("effect"), "effect");
        final JSONObject[] conditionArray = FieldUtils.getObjectArray(effectObject.get("requires"), "requires");

        final EffectAppender[] costAppenders = environment.getEffectAppenderFactory().getEffectAppenders(costArray, environment);
        final EffectAppender[] effectAppenders = environment.getEffectAppenderFactory().getEffectAppenders(effectArray, environment);
        final Requirement[] requirements = environment.getRequirementFactory().getRequirements(conditionArray, environment);

        return new DefaultDelayedAppender() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {

                if(requirementsNotMet(actionContext))
                    return null;

                SubAction subAction = action.createSubAction();

                for (EffectAppender costAppender : costAppenders)
                    costAppender.appendEffect(true, subAction, actionContext);
                for (EffectAppender effectAppender : effectAppenders)
                    effectAppender.appendEffect(false, subAction, actionContext);

                return new StackActionEffect(actionContext.getGame(), subAction);
            }

            private boolean requirementsNotMet(ActionContext actionContext) {
                return (!RequirementUtils.acceptsAllRequirements(requirements, actionContext));
            }

            @Override
            public boolean isPlayableInFull(ActionContext actionContext) {

                if(requirementsNotMet(actionContext))
                    return false;

                for (EffectAppender costAppender : costAppenders) {
                    if (!costAppender.isPlayableInFull(actionContext))
                        return false;
                }

                for (EffectAppender effectAppender : effectAppenders) {
                    if (effectAppender.isPlayabilityCheckedForEffect()
                            && !effectAppender.isPlayableInFull(actionContext))
                        return false;
                }

                return true;
            }
        };
    }
}
