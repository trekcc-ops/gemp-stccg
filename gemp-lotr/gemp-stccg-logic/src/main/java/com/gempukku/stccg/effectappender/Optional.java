package com.gempukku.stccg.effectappender;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.SubAction;
import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
import com.gempukku.stccg.effectappender.resolver.PlayerResolver;
import com.gempukku.stccg.decisions.YesNoDecision;
import com.gempukku.stccg.effects.Effect;
import com.gempukku.stccg.effects.PlayOutDecisionEffect;
import com.gempukku.stccg.effects.StackActionEffect;
import com.gempukku.stccg.rules.GameUtils;
import org.json.simple.JSONObject;

import java.util.Arrays;

public class Optional implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "player", "text", "effect");

        final String player = FieldUtils.getString(effectObject.get("player"), "player", "you");
        final String text = FieldUtils.getString(effectObject.get("text"), "text");
        final JSONObject[] effectArray = FieldUtils.getObjectArray(effectObject.get("effect"), "effect");

        if (text == null)
            throw new InvalidCardDefinitionException("There is a text required for optional effects");

        final PlayerSource playerSource = PlayerResolver.resolvePlayer(player);
        final EffectAppender[] effectAppenders = environment.getEffectAppenderFactory().getEffectAppenders(effectArray, environment);

        return new DefaultDelayedAppender() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                final String choosingPlayer = playerSource.getPlayerId(actionContext);
                SubAction subAction = action.createSubAction();
                subAction.appendCost(
                        new PlayOutDecisionEffect(actionContext.getGame(), choosingPlayer,
                        new YesNoDecision(actionContext.substituteText(_text)) {
                            @Override
                            protected void yes() {
                                Arrays.stream(effectAppenders).forEach(effectAppender ->
                                        effectAppender.appendEffect(
                                                cost, subAction, actionContext.createDelegateContext(choosingPlayer)));
                            }
                        }));
                return new StackActionEffect(actionContext.getGame(), subAction);
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
