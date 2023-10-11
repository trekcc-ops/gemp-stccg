package com.gempukku.stccg.effectappender;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.CardGenerationEnvironment;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.effectappender.resolver.CardResolver;
import com.gempukku.stccg.effects.Effect;
import com.gempukku.stccg.effects.defaulteffect.StackPlayedEventOnACardEffect;
import com.gempukku.stccg.effects.defaulteffect.unrespondable.DoNothingEffect;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
import com.gempukku.stccg.results.PlayEventResult;
import org.json.simple.JSONObject;

public class StackPlayedEvent implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "where");

        final String where = FieldUtils.getString(effectObject.get("where"), "where");

        MultiEffectAppender result = new MultiEffectAppender();

        result.addEffectAppender(
                CardResolver.resolveCard(where, "_temp1", "you", "Choose card to stack on", environment));
        result.addEffectAppender(
                new DefaultDelayedAppender() {
                    @Override
                    protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                        final PhysicalCard card = actionContext.getCardFromMemory("_temp1");
                        if (card != null) {
                            final PlayEventResult playEventResult = (PlayEventResult) actionContext.getEffectResult();
                            return new StackPlayedEventOnACardEffect(actionContext.getGame(), playEventResult.getPlayEventAction(), card);
                        } else
                            return new DoNothingEffect();
                    }
                });

        return result;
    }
}
