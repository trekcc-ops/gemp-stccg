package com.gempukku.stccg.effectappender;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.effects.Effect;
import com.gempukku.stccg.effects.defaulteffect.UnrespondableEffect;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
import com.gempukku.stccg.filters.Filters;
import org.json.simple.JSONObject;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class FilterCardsInMemory implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "filter", "memory", "memorizeMatching", "memorizeNotMatching");

        final String filter = FieldUtils.getString(effectObject.get("filter"), "filter");
        final String memory = FieldUtils.getString(effectObject.get("memory"), "memory");
        final String memorizeMatching = FieldUtils.getString(effectObject.get("memorizeMatching"), "memorizeMatching");
        final String memorizeNotMatching = FieldUtils.getString(effectObject.get("memorizeNotMatching"), "memorizeNotMatching");

        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);

        return new DefaultDelayedAppender() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                return new UnrespondableEffect() {
                    @Override
                    protected void doPlayEffect() {
                        final Filterable filterable = filterableSource.getFilterable(actionContext);
                        final Collection<? extends PhysicalCard> cardsFromMemory = actionContext.getCardsFromMemory(memory);
                        List<PhysicalCard> matchingCards = new LinkedList<>();
                        List<PhysicalCard> notMatchingCards = new LinkedList<>();
                        for (PhysicalCard physicalCard : cardsFromMemory) {
                            if (Filters.and(filterable).accepts(actionContext.getGame(), physicalCard))
                                matchingCards.add(physicalCard);
                            else
                                notMatchingCards.add(physicalCard);
                        }

                        if (memorizeMatching != null)
                            actionContext.setCardMemory(memorizeMatching, matchingCards);
                        if (memorizeNotMatching != null)
                            actionContext.setCardMemory(memorizeNotMatching, notMatchingCards);
                    }
                };
            }
        };
    }
}
