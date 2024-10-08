package com.gempukku.stccg.cards.blueprints.effect.memorize;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.PlayOutDecisionEffect;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.PlayerSource;
import com.gempukku.stccg.cards.blueprints.BlueprintUtils;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.blueprints.effect.DefaultDelayedAppender;
import com.gempukku.stccg.cards.blueprints.effect.EffectAppenderProducer;
import com.gempukku.stccg.cards.blueprints.effect.EffectBlueprint;
import com.gempukku.stccg.decisions.YesNoDecision;

public class ChooseYesOrNo implements EffectAppenderProducer {
    @Override
    public EffectBlueprint createEffectAppender(JsonNode effectObject, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {
        BlueprintUtils.validateAllowedFields(effectObject, "text", "player", "memorize");

        final String text = effectObject.get("text").textValue();
        if (text == null)
            throw new InvalidCardDefinitionException("Text is required for Yes or No decision");
        final String memorize = effectObject.get("memorize").textValue();
        final String yesAnswer = "yes";
        final String noAnswer = "no";
        PlayerSource playerSource = environment.getPlayerSource(effectObject, "player", true);

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