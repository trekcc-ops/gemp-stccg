package com.gempukku.stccg.effectappender;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.cards.CardGenerationEnvironment;
import com.gempukku.stccg.cards.DefaultActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.PlayerSource;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
import com.gempukku.stccg.effectappender.resolver.PlayerResolver;
import com.gempukku.stccg.decisions.YesNoDecision;
import com.gempukku.stccg.effects.Effect;
import com.gempukku.stccg.effects.PlayoutDecisionEffect;
import com.gempukku.stccg.rules.GameUtils;
import org.json.simple.JSONObject;

public class ChooseYesOrNo implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "text", "player", "memorize", "yes", "no");

        final String text = FieldUtils.getString(effectObject.get("text"), "text");
        if (text == null)
            throw new InvalidCardDefinitionException("Text is required for Yes or No decision");
        final String memorize = FieldUtils.getString(effectObject.get("memorize"), "memorize");
        final String yesAnswer = FieldUtils.getString(effectObject.get("yes"), "yes", "yes");
        final String noAnswer = FieldUtils.getString(effectObject.get("no"), "no", "no");
        PlayerSource playerSource = PlayerResolver.resolvePlayer(FieldUtils.getString(effectObject.get("player"), "player", "you"));

        return new DelayedAppender<>() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, DefaultActionContext actionContext) {
                return new PlayoutDecisionEffect(playerSource.getPlayer(actionContext),
                        new YesNoDecision(GameUtils.SubstituteText(text, actionContext)) {
                            @Override
                            protected void yes() {
                                actionContext.setValueToMemory(memorize, GameUtils.SubstituteText(yesAnswer, actionContext));
                            }

                            @Override
                            protected void no() {
                                actionContext.setValueToMemory(memorize, GameUtils.SubstituteText(noAnswer, actionContext));
                            }
                        });
            }
        };
    }
}
