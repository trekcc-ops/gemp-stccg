package com.gempukku.stccg.effectappender;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.SubAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.StackActionEffect;
import com.gempukku.stccg.requirement.Requirement;
import org.json.simple.JSONObject;

public class CostToEffect implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        environment.validateAllowedFields(effectObject, "cost", "effect", "requires");

        final EffectAppender[] costAppenders = environment.getEffectAppendersFromJSON(effectObject,"cost");
        final EffectAppender[] effectAppenders = environment.getEffectAppendersFromJSON(effectObject,"effect");
        final Requirement[] requirements = environment.getRequirementsFromJSON(effectObject);

        return new DefaultDelayedAppender() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext context) {

                if(requirementsNotMet(context))
                    return null;

                SubAction subAction = action.createSubAction();

                for (EffectAppender costAppender : costAppenders)
                    costAppender.appendEffect(true, subAction, context);
                for (EffectAppender effectAppender : effectAppenders)
                    effectAppender.appendEffect(false, subAction, context);

                return new StackActionEffect(context.getGame(), subAction);
            }

            private boolean requirementsNotMet(ActionContext actionContext) {
                return (!actionContext.acceptsAllRequirements(requirements));
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
