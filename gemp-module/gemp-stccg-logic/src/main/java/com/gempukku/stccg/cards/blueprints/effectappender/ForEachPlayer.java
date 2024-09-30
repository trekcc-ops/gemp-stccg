package com.gempukku.stccg.cards.blueprints.effectappender;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.SubAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.StackActionEffect;

import java.util.List;

public class ForEachPlayer implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JsonNode node, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {
        environment.validateAllowedFields(node, "effect");

        final List<EffectAppender> effectAppenders = environment.getEffectAppendersFromJSON(node.get("effect"));

        return new DefaultDelayedAppender() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext context) {
                SubAction subAction = action.createSubAction();
                for (String playerId : context.getGame().getAllPlayerIds()) {
                    effectAppenders.forEach(effectAppender ->
                            effectAppender.appendEffect(cost, action, context.createDelegateContext(playerId)));
                }
                return new StackActionEffect(context.getGame(), subAction);
            }

            @Override
            public boolean isPlayableInFull(ActionContext actionContext) {
                for (String playerId : actionContext.getGame().getAllPlayerIds()) {
                    for (EffectAppender effectAppender : effectAppenders) {
                        if (!effectAppender.isPlayableInFull(actionContext.createDelegateContext(playerId)))
                            return false;
                    }
                }
                return true;
            }
        };
    }
}
