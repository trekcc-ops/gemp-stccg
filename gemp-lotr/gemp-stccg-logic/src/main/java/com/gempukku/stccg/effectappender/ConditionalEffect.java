package com.gempukku.stccg.effectappender;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.SubAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.StackActionEffect;
import com.gempukku.stccg.requirement.Requirement;
import com.gempukku.stccg.requirement.RequirementUtils;
import org.json.simple.JSONObject;

public class ConditionalEffect implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        environment.validateAllowedFields(effectObject, "requires", "effect");

        final Requirement[] conditions = environment.getRequirementsFromJSON(effectObject);
        final EffectAppender[] effectAppenders = environment.getEffectAppendersFromJSON(effectObject,"effect");

        return new DefaultDelayedAppender() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext context) {
                if (checkConditions(context)) {
                    SubAction subAction = action.createSubAction();
                    for (EffectAppender effectAppender : effectAppenders)
                        effectAppender.appendEffect(cost, subAction, context);

                    return new StackActionEffect(context.getGame(), subAction);
                } else {
                    return null;
                }
            }

            private boolean checkConditions(ActionContext actionContext) {
                return RequirementUtils.acceptsAllRequirements(conditions, actionContext);
            }

            @Override
            public boolean isPlayableInFull(ActionContext actionContext) {
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
