package com.gempukku.stccg.cards.blueprints.effectappender;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.UnrespondableEffect;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.blueprints.FilterableSource;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.filters.Filters;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class FilterCardsInMemory implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JsonNode effectObject, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {

        environment.validateAllowedFields(effectObject,
                "filter", "memory", "memorizeMatching", "memorizeNotMatching");

        final String memory = effectObject.get("memory").textValue();
        final String memorizeMatching = effectObject.get("memorizeMatching").textValue();
        final String memorizeNotMatching = effectObject.get("memorizeNotMatching").textValue();

        final FilterableSource filterableSource = environment.getFilterable(effectObject);

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
