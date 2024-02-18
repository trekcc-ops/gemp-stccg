package com.gempukku.stccg.effectappender;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.effects.Effect;
import com.gempukku.stccg.effects.defaulteffect.UnrespondableEffect;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
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

        return new DefaultDelayedAppender() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                return new UnrespondableEffect() {
                    @Override
                    protected void doPlayEffect() {
                        DefaultGame game = actionContext.getGame();
                        final Filterable filterable = filterSource.getFilterable(actionContext);
                        final Filterable onFilterable = onFilterSource.getFilterable(actionContext);
                        final Collection<PhysicalCard> cardsWithStack = Filters.filterActive(game, onFilterable);

                        List<PhysicalCard> cardsToMemorize = new LinkedList<>();
                        for (PhysicalCard cardWithStack : cardsWithStack)
                            cardsToMemorize.addAll(Filters.filter(cardWithStack.getStackedCards(), game, filterable));
                        actionContext.setCardMemory(memory, cardsToMemorize);
                    }
                };
            }
        };
    }

}
