package com.gempukku.stccg.effectappender;

import com.gempukku.stccg.cards.DefaultActionContext;
import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.SubAction;
import com.gempukku.stccg.cards.CardGenerationEnvironment;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.effects.Effect;
import com.gempukku.stccg.effects.StackActionEffect;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.requirement.Requirement;
import com.gempukku.stccg.requirement.RequirementUtils;
import org.json.simple.JSONObject;

public class ConditionalEffect implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "requires", "effect");

        final JSONObject[] conditionArray = FieldUtils.getObjectArray(effectObject.get("requires"), "requires");
        final JSONObject[] effectArray = FieldUtils.getObjectArray(effectObject.get("effect"), "effect");

        final Requirement[] conditions = environment.getRequirementFactory().getRequirements(conditionArray, environment);
        final EffectAppender[] effectAppenders = environment.getEffectAppenderFactory().getEffectAppenders(effectArray, environment);

        return new DelayedAppender<>() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, DefaultActionContext actionContext) {
                if (checkConditions(actionContext)) {
                    SubAction subAction = new SubAction(action);
                    for (EffectAppender effectAppender : effectAppenders)
                        effectAppender.appendEffect(cost, subAction, actionContext);

                    return new StackActionEffect(subAction);
                } else {
                    return null;
                }
            }

            private boolean checkConditions(DefaultActionContext<DefaultGame> actionContext) {
                return RequirementUtils.acceptsAllRequirements(conditions, actionContext);
            }

            @Override
            public boolean isPlayableInFull(DefaultActionContext<DefaultGame> actionContext) {
                if (!checkConditions(actionContext))
                    return false;
                for (EffectAppender effectAppender : effectAppenders) {
                    if (!effectAppender.isPlayableInFull(actionContext))
                        return false;
                }

                return true;
            }
        };
    }

}
