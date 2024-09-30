package com.gempukku.stccg.cards.blueprints.effectappender;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.SubAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.StackActionEffect;
import com.gempukku.stccg.requirement.Requirement;

import java.util.List;

public class ConditionalEffect implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JsonNode node, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        environment.validateAllowedFields(node, "requires", "effect");

        final Requirement[] conditions = environment.getRequirementsFromJSON(node);
        final List<EffectAppender> effectAppenders = environment.getEffectAppendersFromJSON(node.get("effect"));

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
                return actionContext.acceptsAllRequirements(conditions);
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
