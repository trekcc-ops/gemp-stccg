package com.gempukku.stccg.effectappender;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.effectappender.resolver.CardResolver;
import com.gempukku.stccg.effectappender.resolver.TimeResolver;
import com.gempukku.stccg.effectappender.resolver.ValueResolver;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.turn.AddUntilModifierEffect;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.modifiers.RemoveGameTextModifier;
import org.json.simple.JSONObject;

import java.util.Collection;

public class RemoveText implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        environment.validateAllowedFields(effectObject, "count", "filter", "until", "memorize");

        final ValueSource valueSource = ValueResolver.resolveEvaluator(effectObject.get("count"), 1, environment);
        final String filter = environment.getString(effectObject.get("filter"), "filter");
        final String memory = environment.getString(effectObject.get("memorize"), "memorize", "_temp");
        final TimeResolver.Time time = TimeResolver.resolveTime(effectObject.get("until"), "end(current)");

        MultiEffectAppender result = new MultiEffectAppender();

        result.addEffectAppender(
                CardResolver.resolveCards(filter, valueSource, memory, "you", "Choose cards to remove text from", environment));
        result.addEffectAppender(
                new DefaultDelayedAppender() {
                    @Override
                    protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext context) {
                        final Collection<? extends PhysicalCard> cardsFromMemory = context.getCardsFromMemory(memory);
                        return new AddUntilModifierEffect(context.getGame(),
                                new RemoveGameTextModifier(context.getSource(), Filters.in(cardsFromMemory)), time);
                    }
                });

        return result;
    }

}
