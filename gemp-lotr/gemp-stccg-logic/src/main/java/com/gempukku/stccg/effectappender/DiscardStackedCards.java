package com.gempukku.stccg.effectappender;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.effectappender.resolver.CardResolver;
import com.gempukku.stccg.effectappender.resolver.ValueResolver;
import com.gempukku.stccg.effects.Effect;
import com.gempukku.stccg.effects.defaulteffect.DiscardCardsFromZoneEffect;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
import org.json.simple.JSONObject;

public class DiscardStackedCards implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "on", "filter", "count" ,"memorize");

        String on = FieldUtils.getString(effectObject.get("on"), "on", "any");
        String filter = FieldUtils.getString(effectObject.get("filter"), "filter", "choose(any)");
        final ValueSource valueSource = ValueResolver.resolveEvaluator(effectObject.get("count"), 1, environment);
        final String memory = FieldUtils.getString(effectObject.get("memorize"), "memorize", "_temp");

        final FilterableSource onFilterSource = environment.getFilterFactory().generateFilter(on, environment);

        MultiEffectAppender result = new MultiEffectAppender();
        result.addEffectAppender(
                CardResolver.resolveStackedCards(filter, valueSource, onFilterSource, memory, "you", "Choose stacked cards to discard", environment));
        result.addEffectAppender(
                new DefaultDelayedAppender() {
                    @Override
                    protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                        return new DiscardCardsFromZoneEffect(actionContext, Zone.STACKED, actionContext.getCardsFromMemory(memory));
                    }
                });
        return result;
    }

}
