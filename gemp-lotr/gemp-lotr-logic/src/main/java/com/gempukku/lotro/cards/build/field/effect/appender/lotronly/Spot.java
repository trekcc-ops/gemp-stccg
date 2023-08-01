package com.gempukku.lotro.cards.build.field.effect.appender.lotronly;

import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.EffectAppender;
import com.gempukku.lotro.cards.build.field.effect.EffectAppenderProducer;
import com.gempukku.lotro.cards.build.field.effect.appender.DelayedAppender;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.CardResolver;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.ValueResolver;
import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.actions.lotronly.CostToEffectAction;
import com.gempukku.lotro.effects.Effect;
import com.gempukku.lotro.game.PlayConditions;
import com.gempukku.lotro.effects.UnrespondableEffect;
import org.json.simple.JSONObject;

public class Spot implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "count", "filter", "memorize", "text", "skipMemorize");

        final ValueSource valueSource = ValueResolver.resolveEvaluator(effectObject.get("count"), 1, environment);
        final String filter = FieldUtils.getString(effectObject.get("filter"), "filter");
        final String memorize = FieldUtils.getString(effectObject.get("memorize"), "memorize", "_temp");
        final String text = FieldUtils.getString(effectObject.get("text"), "text", "Choose card to spot");
        final boolean skipMemorize = FieldUtils.getBoolean(effectObject.get("skipMemorize"), "skipMemorize", false);

        if (skipMemorize) {
            final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);

            return new DelayedAppender<>() {
                @Override
                protected Effect createEffect(boolean cost, CostToEffectAction action, DefaultActionContext actionContext) {
                    return new UnrespondableEffect() {
                        @Override
                        protected void doPlayEffect(DefaultGame game) {
                        }
                    };
                }

                @Override
                public boolean isPlayableInFull(DefaultActionContext<DefaultGame> actionContext) {
                    final int count = valueSource.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), null);
                    Filterable filterable = filterableSource.getFilterable(actionContext);
                    return PlayConditions.canSpot(actionContext.getGame(), count, filterable);
                }
            };
        } else {
            return CardResolver.resolveCards(
                    filter, valueSource, memorize, "you", text, environment);
        }
    }
}