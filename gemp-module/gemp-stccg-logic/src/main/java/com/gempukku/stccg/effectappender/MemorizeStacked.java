package com.gempukku.stccg.effectappender;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.UnrespondableEffect;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.FilterableSource;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import org.json.simple.JSONObject;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class MemorizeStacked implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        environment.validateAllowedFields(effectObject, "filter", "on", "memory");

        final String filter = environment.getString(effectObject.get("filter"), "filter", "any");
        final String on = environment.getString(effectObject.get("on"), "on");
        final String memory = environment.getString(effectObject.get("memory"), "memory");

        final FilterableSource filterSource = environment.getFilterFactory().generateFilter(filter);
        final FilterableSource onFilterSource = environment.getFilterFactory().generateFilter(on);

        return new DefaultDelayedAppender() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext context) {
                return new UnrespondableEffect(context) {
                    @Override
                    protected void doPlayEffect() {
                        DefaultGame game = context.getGame();
                        final Filterable filterable = filterSource.getFilterable(context);
                        final Filterable onFilterable = onFilterSource.getFilterable(context);
                        final Collection<PhysicalCard> cardsWithStack = Filters.filterActive(game, onFilterable);

                        List<PhysicalCard> cardsToMemorize = new LinkedList<>();
                        for (PhysicalCard cardWithStack : cardsWithStack)
                            cardsToMemorize.addAll(Filters.filter(cardWithStack.getStackedCards(), game, filterable));
                        context.setCardMemory(memory, cardsToMemorize);
                    }
                };
            }
        };
    }

}
