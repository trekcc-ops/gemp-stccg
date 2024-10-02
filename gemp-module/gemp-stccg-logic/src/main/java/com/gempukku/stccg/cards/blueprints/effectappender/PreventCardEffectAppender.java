package com.gempukku.stccg.cards.blueprints.effectappender;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.PlayerSource;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.blueprints.FilterableSource;
import com.gempukku.stccg.cards.blueprints.resolver.CardResolver;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.PreventableCardEffect;
import com.gempukku.stccg.actions.PreventCardEffect;
import com.gempukku.stccg.filters.Filters;

public class PreventCardEffectAppender implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JsonNode effectObject, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        environment.validateAllowedFields(effectObject, "filter", "memorize");

        String filter = effectObject.get("filter").textValue();
        final String memory = environment.getString(effectObject, "memorize", "_temp");
        PlayerSource you = ActionContext::getPerformingPlayerId;
        FilterableSource cardFilter = environment.getCardFilterableIfChooseOrAll(filter);

        MultiEffectAppender result = new MultiEffectAppender();
        result.addEffectAppender(
                CardResolver.resolveCardInPlay(filter,
                        (actionContext) -> Filters.in(((PreventableCardEffect) actionContext.getEffect()).getAffectedCardsMinusPrevented()),
                        memory, you, "Choose card to prevent effect on", cardFilter));
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
