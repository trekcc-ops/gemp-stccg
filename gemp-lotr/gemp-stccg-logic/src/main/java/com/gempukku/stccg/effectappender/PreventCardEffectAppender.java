package com.gempukku.stccg.effectappender;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.CardGenerationEnvironment;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.effectappender.resolver.CardResolver;
import com.gempukku.stccg.effects.Effect;
import com.gempukku.stccg.effects.PreventableCardEffect;
import com.gempukku.stccg.effects.defaulteffect.unrespondable.PreventCardEffect;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
import com.gempukku.stccg.filters.Filters;
import org.json.simple.JSONObject;

public class PreventCardEffectAppender implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "filter", "memorize");

        String filter = FieldUtils.getString(effectObject.get("filter"), "filter");
        final String memory = FieldUtils.getString(effectObject.get("memorize"), "memorize", "_temp");

        MultiEffectAppender result = new MultiEffectAppender();
        result.addEffectAppender(
                CardResolver.resolveCard(filter,
                        (actionContext) -> Filters.in(((PreventableCardEffect) actionContext.getEffect()).getAffectedCardsMinusPrevented(actionContext.getGame())),
                        memory, "you", "Choose card to prevent effect on", environment));
        result.addEffectAppender(
                new DefaultDelayedAppender() {
                    @Override
                    protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                        return new PreventCardEffect(actionContext.getGame(), (PreventableCardEffect) actionContext.getEffect(), Filters.in(actionContext.getCardsFromMemory(memory)));
                    }
                });

        return result;
    }

}
