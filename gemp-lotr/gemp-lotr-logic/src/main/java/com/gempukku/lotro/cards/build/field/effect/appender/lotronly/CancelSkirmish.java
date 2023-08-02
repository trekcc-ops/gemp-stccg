package com.gempukku.lotro.cards.build.field.effect.appender.lotronly;

import com.gempukku.lotro.actions.lotronly.CostToEffectAction;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.DefaultActionContext;
import com.gempukku.lotro.cards.build.FilterableSource;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.appender.EffectAppender;
import com.gempukku.lotro.cards.build.field.effect.appender.EffectAppenderProducer;
import com.gempukku.lotro.cards.build.field.effect.appender.DelayedAppender;
import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.effects.CancelSkirmishEffect;
import com.gempukku.lotro.effects.Effect;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.DefaultGame;
import org.json.simple.JSONObject;

public class CancelSkirmish implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "filter");

        final String filter = FieldUtils.getString(effectObject.get("filter"), "filter", "any");

        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);

        return new DelayedAppender<>() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, DefaultActionContext actionContext) {
                final Filterable involving = filterableSource.getFilterable(actionContext);
                return new CancelSkirmishEffect(involving);
            }

            @Override
            public boolean isPlayableInFull(DefaultActionContext<DefaultGame> actionContext) {
                final Filterable involving = filterableSource.getFilterable(actionContext);
                final DefaultGame game = actionContext.getGame();
                return game.getGameState().getSkirmish() != null
                        && !game.getGameState().getSkirmish().isCancelled()
                        && (Filters.countActive(game, Filters.and(involving, Filters.inSkirmish)) > 0)
                        && game.getModifiersQuerying().canCancelSkirmish(game, game.getGameState().getSkirmish().getFellowshipCharacter())
                        && (game.getFormat().canCancelRingBearerSkirmish() || Filters.countActive(game, Filters.ringBearer, Filters.inSkirmish) == 0);
            }
        };
    }
}
