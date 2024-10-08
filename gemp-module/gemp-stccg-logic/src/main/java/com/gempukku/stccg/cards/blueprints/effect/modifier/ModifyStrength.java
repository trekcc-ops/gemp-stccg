package com.gempukku.stccg.cards.blueprints.effect.modifier;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.blueprints.FilterableSource;
import com.gempukku.stccg.cards.blueprints.ValueSource;
import com.gempukku.stccg.cards.blueprints.effect.DefaultDelayedAppender;
import com.gempukku.stccg.cards.blueprints.effect.EffectBlueprint;
import com.gempukku.stccg.cards.blueprints.effect.EffectAppenderProducer;
import com.gempukku.stccg.cards.blueprints.effect.MultiEffectBlueprint;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.blueprints.resolver.CardResolver;
import com.gempukku.stccg.cards.blueprints.resolver.TimeResolver;
import com.gempukku.stccg.cards.blueprints.resolver.ValueResolver;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.turn.AddUntilModifierEffect;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.modifiers.attributes.StrengthModifier;

import java.util.Collection;

public class ModifyStrength implements EffectAppenderProducer {
    @Override
    public EffectBlueprint createEffectAppender(JsonNode effectObject, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {
        environment.validateAllowedFields(effectObject, "amount", "count", "filter", "until", "memorize");

        final ValueSource amountSource = ValueResolver.resolveEvaluator(effectObject.get("amount"));
        final ValueSource valueSource = ValueResolver.resolveEvaluator(effectObject.get("count"), 1, environment);
        final String filter = effectObject.get("filter").textValue();
        final String memory = environment.getString(effectObject, "memorize", "_temp");
        final TimeResolver.Time time = TimeResolver.resolveTime(effectObject.get("until"), "end(current)");
        final PlayerSource you = ActionContext::getPerformingPlayerId;

        MultiEffectBlueprint result = new MultiEffectBlueprint();
        FilterableSource cardFilter = environment.getCardFilterableIfChooseOrAll(filter);

        result.addEffectAppender(
                CardResolver.resolveCardsInPlay(filter, valueSource, memory, you,
                        "Choose cards to modify strength of", cardFilter));
        result.addEffectAppender(
                new DefaultDelayedAppender() {
                    @Override
                    protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext context) {
                        final Collection<? extends PhysicalCard> cardsFromMemory = context.getCardsFromMemory(memory);
                        final int amount = amountSource.evaluateExpression(context, context.getSource());
                        return new AddUntilModifierEffect(context.getGame(),
                                new StrengthModifier(context, Filters.in(cardsFromMemory), amount), time);
                    }
                });

        return result;
    }

}