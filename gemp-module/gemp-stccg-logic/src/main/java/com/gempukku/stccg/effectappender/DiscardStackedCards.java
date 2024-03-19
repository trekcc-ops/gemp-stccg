package com.gempukku.stccg.effectappender;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.discard.DiscardCardsFromZoneEffect;
import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.effectappender.resolver.CardResolver;
import com.gempukku.stccg.effectappender.resolver.ValueResolver;
import org.json.simple.JSONObject;

public class DiscardStackedCards implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        environment.validateAllowedFields(effectObject, "on", "filter", "count" ,"memorize");

        String on = environment.getString(effectObject.get("on"), "on", "any");
        String filter = environment.getString(effectObject.get("filter"), "filter", "choose(any)");
        final ValueSource valueSource = ValueResolver.resolveEvaluator(effectObject.get("count"), 1, environment);
        final String memory = environment.getString(effectObject.get("memorize"), "memorize", "_temp");

        final FilterableSource onFilterSource = environment.getFilterFactory().generateFilter(on);

        MultiEffectAppender result = new MultiEffectAppender();
        result.addEffectAppender(
                CardResolver.resolveStackedCards(filter, valueSource, onFilterSource, memory, "you", "Choose stacked cards to discard", environment));
        result.addEffectAppender(
                new DefaultDelayedAppender() {
                    @Override
                    protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext context) {
                        return new DiscardCardsFromZoneEffect(context, Zone.STACKED, context.getCardsFromMemory(memory));
                    }
                });
        return result;
    }

}
