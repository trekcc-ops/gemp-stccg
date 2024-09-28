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
import org.json.simple.JSONObject;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class FilterCardsInMemory implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        environment.validateAllowedFields(effectObject, "filter", "memory", "memorizeMatching", "memorizeNotMatching");

        final String filter = environment.getString(effectObject.get("filter"), "filter");
        final String memory = environment.getString(effectObject.get("memory"), "memory");
        final String memorizeMatching = environment.getString(effectObject.get("memorizeMatching"), "memorizeMatching");
        final String memorizeNotMatching = environment.getString(effectObject.get("memorizeNotMatching"), "memorizeNotMatching");

        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter);

        return new DefaultDelayedAppender() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext context) {
                return new UnrespondableEffect(context) {
                    @Override
                    protected void doPlayEffect() {
                        final Filterable filterable = filterableSource.getFilterable(context);
                        final Collection<? extends PhysicalCard> cardsFromMemory = context.getCardsFromMemory(memory);
                        List<PhysicalCard> matchingCards = new LinkedList<>();
                        List<PhysicalCard> notMatchingCards = new LinkedList<>();
                        for (PhysicalCard physicalCard : cardsFromMemory) {
                            if (Filters.and(filterable).accepts(context.getGame(), physicalCard))
                                matchingCards.add(physicalCard);
                            else
                                notMatchingCards.add(physicalCard);
                        }

                        if (memorizeMatching != null)
                            context.setCardMemory(memorizeMatching, matchingCards);
                        if (memorizeNotMatching != null)
                            context.setCardMemory(memorizeNotMatching, notMatchingCards);
                    }
                };
            }
        };
    }
}
