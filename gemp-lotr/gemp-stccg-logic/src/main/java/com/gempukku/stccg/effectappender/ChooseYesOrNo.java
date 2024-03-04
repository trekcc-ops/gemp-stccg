package com.gempukku.stccg.effectappender;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.decisions.YesNoDecision;
import com.gempukku.stccg.effectappender.resolver.PlayerResolver;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.PlayOutDecisionEffect;
import org.json.simple.JSONObject;

public class ChooseYesOrNo implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        environment.validateAllowedFields(effectObject, "text", "player", "memorize", "yes", "no");

        final String text = environment.getString(effectObject.get("text"), "text");
        if (text == null)
            throw new InvalidCardDefinitionException("Text is required for Yes or No decision");
        final String memorize = environment.getString(effectObject.get("memorize"), "memorize");
        final String yesAnswer = environment.getString(effectObject.get("yes"), "yes", "yes");
        final String noAnswer = environment.getString(effectObject.get("no"), "no", "no");
        PlayerSource playerSource = PlayerResolver.resolvePlayer(environment.getString(effectObject.get("player"), "player", "you"));

        return new DefaultDelayedAppender() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext context) {
                return new PlayOutDecisionEffect(context.getGame(), playerSource.getPlayerId(context),
                        new YesNoDecision(context.substituteText(_text)) {
                            @Override
                            protected void yes() {
                                context.setValueToMemory(memorize, context.substituteText(yesAnswer));
                            }

                            @Override
                            protected void no() {
                                context.setValueToMemory(memorize, context.substituteText(noAnswer));
                            }
                        });
            }
        };
    }
}
