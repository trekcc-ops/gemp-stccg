package com.gempukku.stccg.effectappender;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.SubAction;
import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.effectappender.resolver.PlayerResolver;
import com.gempukku.stccg.decisions.YesNoDecision;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.PlayOutDecisionEffect;
import com.gempukku.stccg.actions.StackActionEffect;
import org.json.simple.JSONObject;

import java.util.Arrays;

public class Optional implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        environment.validateAllowedFields(effectObject, "player", "text", "effect");

        final String text = environment.getString(effectObject.get("text"), "text");

        if (text == null)
            throw new InvalidCardDefinitionException("There is a text required for optional effects");

        final PlayerSource playerSource = PlayerResolver.resolvePlayer(environment.getString(effectObject.get("player"), "player", "you"));
        final EffectAppender[] effectAppenders = environment.getEffectAppendersFromJSON(effectObject,"effect");

        return new DefaultDelayedAppender() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext context) {
                final String choosingPlayer = playerSource.getPlayerId(context);
                SubAction subAction = action.createSubAction();
                subAction.appendCost(
                        new PlayOutDecisionEffect(context.getGame(), choosingPlayer,
                        new YesNoDecision(context.substituteText(_text)) {
                            @Override
                            protected void yes() {
                                Arrays.stream(effectAppenders).forEach(effectAppender ->
                                        effectAppender.appendEffect(
                                                cost, subAction, context.createDelegateContext(choosingPlayer)));
                            }
                        }));
                return new StackActionEffect(context.getGame(), subAction);
            }

            @Override
            public boolean isPlayableInFull(ActionContext actionContext) {
                final String choosingPlayer = playerSource.getPlayerId(actionContext);
                for (EffectAppender effectAppender : effectAppenders) {
                    if (!effectAppender.isPlayableInFull(actionContext.createDelegateContext(choosingPlayer)))
                        return false;
                }

                return true;
            }

            @Override
            public boolean isPlayabilityCheckedForEffect() {
                return Arrays.stream(effectAppenders).anyMatch(EffectAppender::isPlayabilityCheckedForEffect);
            }
        };
    }
}
