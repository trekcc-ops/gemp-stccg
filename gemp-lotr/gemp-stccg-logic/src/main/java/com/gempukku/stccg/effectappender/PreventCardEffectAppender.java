package com.gempukku.stccg.effectappender;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.effectappender.resolver.CardResolver;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.PreventableCardEffect;
import com.gempukku.stccg.actions.PreventCardEffect;
import com.gempukku.stccg.filters.Filters;
import org.json.simple.JSONObject;

public class PreventCardEffectAppender implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        environment.validateAllowedFields(effectObject, "filter", "memorize");

        String filter = environment.getString(effectObject.get("filter"), "filter");
        final String memory = environment.getString(effectObject.get("memorize"), "memorize", "_temp");

        MultiEffectAppender result = new MultiEffectAppender();
        result.addEffectAppender(
                CardResolver.resolveCard(filter,
                        (actionContext) -> Filters.in(((PreventableCardEffect) actionContext.getEffect()).getAffectedCardsMinusPrevented()),
                        memory, "you", "Choose card to prevent effect on", environment));
        result.addEffectAppender(
                new DefaultDelayedAppender() {
                    @Override
                    protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext context) {
                        return new PreventCardEffect(context.getGame(), (PreventableCardEffect) context.getEffect(), Filters.in(context.getCardsFromMemory(memory)));
                    }
                });

        return result;
    }

}
