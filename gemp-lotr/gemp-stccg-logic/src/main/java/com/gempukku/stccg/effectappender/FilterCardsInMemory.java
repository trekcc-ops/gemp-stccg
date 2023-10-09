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

public class FilterCardsInMemory implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "filter", "memory", "memorizeMatching", "memorizeNotMatching");

        final String filter = FieldUtils.getString(effectObject.get("filter"), "filter");
        final String memory = FieldUtils.getString(effectObject.get("memory"), "memory");
        final String memorizeMatching = FieldUtils.getString(effectObject.get("memorizeMatching"), "memorizeMatching");
        final String memorizeNotMatching = FieldUtils.getString(effectObject.get("memorizeNotMatching"), "memorizeNotMatching");

        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);

        return new DelayedAppender<>() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, DefaultActionContext actionContext) {
                return new UnrespondableEffect() {
                    @Override
                    protected void doPlayEffect(DefaultGame game) {
                        final Filterable filterable = filterableSource.getFilterable(actionContext);
                        final Collection<? extends PhysicalCard> cardsFromMemory = actionContext.getCardsFromMemory(memory);
                        List<PhysicalCard> matchingCards = new LinkedList<>();
                        List<PhysicalCard> notMatchingCards = new LinkedList<>();
                        for (PhysicalCard physicalCard : cardsFromMemory) {
                            if (Filters.and(filterable).accepts(game, physicalCard))
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
