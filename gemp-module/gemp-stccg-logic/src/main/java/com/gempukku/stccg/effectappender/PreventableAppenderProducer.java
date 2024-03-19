package com.gempukku.stccg.effectappender;

import com.gempukku.stccg.actions.*;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.PlayerSource;
import com.gempukku.stccg.decisions.YesNoDecision;
import com.gempukku.stccg.effectappender.resolver.PlayerResolver;
import org.json.simple.JSONObject;

import java.util.Arrays;

public class PreventableAppenderProducer implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        environment.validateAllowedFields(effectObject, "text", "player", "effect", "cost");

        final String text = environment.getString(effectObject.get("text"), "text");
        final String player = environment.getString(effectObject.get("player"), "player");

        if (text == null)
            throw new InvalidCardDefinitionException("Text is required for preventable effect");
        if (player == null)
            throw new InvalidCardDefinitionException("Player is required for preventable effect");

        final PlayerSource preventingPlayerSource = PlayerResolver.resolvePlayer(player);
        final EffectAppender[] effectAppenders = environment.getEffectAppendersFromJSON(effectObject,"effect");
        final EffectAppender[] costAppenders = environment.getEffectAppendersFromJSON(effectObject,"cost");

        return new DefaultDelayedAppender() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext context) {
                if (areCostsPlayable(context)) {
                    final String preventingPlayer = preventingPlayerSource.getPlayerId(context);

                    SubAction subAction = action.createSubAction();
                    subAction.appendEffect(
                            new PlayOutDecisionEffect(context.getGame(), preventingPlayer,
                                    new YesNoDecision(context.substituteText(_text)) {
                                        @Override
                                        protected void yes() {
                                            ActionContext delegate = context.createDelegateContext(preventingPlayer);
                                            for (EffectAppender costAppender : costAppenders)
                                                costAppender.appendEffect(false, subAction, delegate);

                                            subAction.appendEffect(
                                                    new UnrespondableEffect() {
                                                        @Override
                                                        protected void doPlayEffect() {
                                                            // If the prevention was not carried out, need to do the original action anyway
                                                            if (!subAction.wasCarriedOut()) {
                                                                for (EffectAppender effectAppender : effectAppenders)
                                                                    effectAppender.appendEffect(false, subAction, context);
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
                                                effectAppender.appendEffect(false, subAction, context);
                                        }
                                    }));
                    return new StackActionEffect(context.getGame(), subAction);
                } else {
                    SubAction subAction = action.createSubAction();
                    for (EffectAppender effectAppender : effectAppenders)
                        effectAppender.appendEffect(false, subAction, context);
                    return new StackActionEffect(context.getGame(), subAction);
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
