package com.gempukku.stccg.effectappender;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.SubAction;
import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.decisions.YesNoDecision;
import com.gempukku.stccg.effectappender.resolver.PlayerResolver;
import com.gempukku.stccg.effects.Effect;
import com.gempukku.stccg.effects.PlayOutDecisionEffect;
import com.gempukku.stccg.effects.StackActionEffect;
import com.gempukku.stccg.effects.defaulteffect.UnrespondableEffect;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
import com.gempukku.stccg.rules.GameUtils;
import org.json.simple.JSONObject;

import java.util.Arrays;

public class PreventableAppenderProducer implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "text", "player", "effect", "cost");

        final String text = FieldUtils.getString(effectObject.get("text"), "text");
        final String player = FieldUtils.getString(effectObject.get("player"), "player");
        JSONObject[] effectArray = FieldUtils.getObjectArray(effectObject.get("effect"), "effect");
        JSONObject[] costArray = FieldUtils.getObjectArray(effectObject.get("cost"), "cost");

        if (text == null)
            throw new InvalidCardDefinitionException("Text is required for preventable effect");
        if (player == null)
            throw new InvalidCardDefinitionException("Player is required for preventable effect");

        final PlayerSource preventingPlayerSource = PlayerResolver.resolvePlayer(player);
        final EffectAppender[] effectAppenders = environment.getEffectAppenderFactory().getEffectAppenders(effectArray, environment);
        final EffectAppender[] costAppenders = environment.getEffectAppenderFactory().getEffectAppenders(costArray, environment);

        return new DefaultDelayedAppender() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                if (areCostsPlayable(actionContext)) {
                    final String preventingPlayer = preventingPlayerSource.getPlayerId(actionContext);

                    SubAction subAction = action.createSubAction();
                    subAction.appendEffect(
                            new PlayOutDecisionEffect(actionContext.getGame(), preventingPlayer,
                                    new YesNoDecision(actionContext.substituteText(_text)) {
                                        @Override
                                        protected void yes() {
                                            ActionContext delegate = actionContext.createDelegateContext(preventingPlayer);
                                            for (EffectAppender costAppender : costAppenders)
                                                costAppender.appendEffect(false, subAction, delegate);

                                            subAction.appendEffect(
                                                    new UnrespondableEffect() {
                                                        @Override
                                                        protected void doPlayEffect() {
                                                            // If the prevention was not carried out, need to do the original action anyway
                                                            if (!subAction.wasCarriedOut()) {
                                                                for (EffectAppender effectAppender : effectAppenders)
                                                                    effectAppender.appendEffect(false, subAction, actionContext);
                                                            }
                                                        }

                                                        @Override
                                                        public boolean wasCarriedOut() {
                                                            // Cheating a bit, we need to check, if the preventing effect was carried out,
                                                            // but have no way of doing this, as we can do that through subAction only,
                                                            // and this checking effect should be ALWAYS considered fine, even before it
                                                            // was done
                                                            return true;
                                                        }
                                                    });
                                        }

                                        @Override
                                        protected void no() {
                                            for (EffectAppender effectAppender : effectAppenders)
                                                effectAppender.appendEffect(false, subAction, actionContext);
                                        }
                                    }));
                    return new StackActionEffect(actionContext.getGame(), subAction);
                } else {
                    SubAction subAction = action.createSubAction();
                    for (EffectAppender effectAppender : effectAppenders)
                        effectAppender.appendEffect(false, subAction, actionContext);
                    return new StackActionEffect(actionContext.getGame(), subAction);
                }
            }

            private boolean areCostsPlayable(ActionContext actionContext) {
                return Arrays.stream(costAppenders).allMatch(costAppender -> costAppender.isPlayableInFull(actionContext));
            }

            @Override
            public boolean isPlayableInFull(ActionContext actionContext) {
                return Arrays.stream(effectAppenders).allMatch(effectAppender -> effectAppender.isPlayableInFull(actionContext));
            }
        };
    }
}
