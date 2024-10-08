package com.gempukku.stccg.cards.blueprints.effect;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.PreventCardEffect;
import com.gempukku.stccg.actions.PreventableCardEffect;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.ConstantValueSource;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.blueprints.BlueprintUtils;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.blueprints.FilterableSource;
import com.gempukku.stccg.cards.blueprints.resolver.CardResolver;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.filters.Filters;

import java.util.List;
import java.util.function.Function;

public class PreventCardEffectAppender implements EffectAppenderProducer {
    @Override
    public EffectBlueprint createEffectAppender(JsonNode effectObject, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        BlueprintUtils.validateAllowedFields(effectObject, "filter", "memorize");

        String filter = effectObject.get("filter").textValue();
        final String memory = BlueprintUtils.getString(effectObject, "memorize", "_temp");
        FilterableSource cardFilter = environment.getCardFilterableIfChooseOrAll(filter);

        MultiEffectBlueprint result = new MultiEffectBlueprint();

        Function<ActionContext, List<PhysicalCard>> cardSource =
                actionContext -> Filters.filterActive(actionContext.getGame(), Filters.any).stream().toList();
        FilterableSource choiceFilter = (actionContext) ->
                Filters.in(((PreventableCardEffect) actionContext.getEffect()).getAffectedCardsMinusPrevented());

        EffectBlueprint targetCardAppender = CardResolver.resolveCardsInPlay(filter, cardFilter, choiceFilter,
                choiceFilter, new ConstantValueSource(1), memory, ActionContext::getPerformingPlayerId,
                "Choose card to prevent effect on", cardSource);

        result.addEffectAppender(targetCardAppender);
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