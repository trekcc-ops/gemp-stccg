package com.gempukku.stccg.cards.blueprints.effectappender;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.decisions.YesNoDecision;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.PlayOutDecisionEffect;

public class ChooseYesOrNo implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JsonNode effectObject, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {
        environment.validateAllowedFields(effectObject, "text", "player", "memorize", "yes", "no");

        final String text = effectObject.get("text").textValue();
        if (text == null)
            throw new InvalidCardDefinitionException("Text is required for Yes or No decision");
        final String memorize = effectObject.get("memorize").textValue();
        final String yesAnswer = environment.getString(effectObject, "yes", "yes");
        final String noAnswer = environment.getString(effectObject, "no", "no");
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
