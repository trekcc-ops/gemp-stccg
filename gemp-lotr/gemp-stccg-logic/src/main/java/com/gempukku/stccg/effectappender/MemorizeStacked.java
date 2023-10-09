package com.gempukku.stccg.effectappender;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.cards.CardGenerationEnvironment;
import com.gempukku.stccg.cards.DefaultActionContext;
import com.gempukku.stccg.cards.FilterableSource;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.effects.Effect;
import com.gempukku.stccg.effects.UnrespondableEffect;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import org.json.simple.JSONObject;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class MemorizeStacked implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "filter", "on", "memory");

        final String filter = FieldUtils.getString(effectObject.get("filter"), "filter", "any");
        final String on = FieldUtils.getString(effectObject.get("on"), "on");
        final String memory = FieldUtils.getString(effectObject.get("memory"), "memory");

        final FilterableSource filterSource = environment.getFilterFactory().generateFilter(filter, environment);
        final FilterableSource onFilterSource = environment.getFilterFactory().generateFilter(on, environment);

        return new DelayedAppender<>() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, DefaultActionContext actionContext) {
                return new UnrespondableEffect() {
                    @Override
                    protected void doPlayEffect(DefaultGame game) {
                        final Filterable filterable = filterSource.getFilterable(actionContext);
                        final Filterable onFilterable = onFilterSource.getFilterable(actionContext);
                        final Collection<PhysicalCard> cardsWithStack = Filters.filterActive(game, onFilterable);

                        List<PhysicalCard> cardsToMemorize = new LinkedList<>();
                        for (PhysicalCard cardWithStack : cardsWithStack)
                            cardsToMemorize.addAll(Filters.filter(game.getGameState().getStackedCards(cardWithStack), game, filterable));
                        actionContext.setCardMemory(memory, cardsToMemorize);
                    }
                };
            }
        };
    }

}
