package com.gempukku.lotro.effectappender;

import com.gempukku.lotro.actions.CostToEffectAction;
import com.gempukku.lotro.cards.CardGenerationEnvironment;
import com.gempukku.lotro.actioncontext.DefaultActionContext;
import com.gempukku.lotro.cards.FilterableSource;
import com.gempukku.lotro.cards.InvalidCardDefinitionException;
import com.gempukku.lotro.fieldprocessor.FieldUtils;
import com.gempukku.lotro.cards.LotroPhysicalCard;
import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.effects.Effect;
import com.gempukku.lotro.effects.UnrespondableEffect;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.DefaultGame;
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
                        final Collection<? extends LotroPhysicalCard> cardsFromMemory = actionContext.getCardsFromMemory(memory);
                        List<LotroPhysicalCard> matchingCards = new LinkedList<>();
                        List<LotroPhysicalCard> notMatchingCards = new LinkedList<>();
                        for (LotroPhysicalCard physicalCard : cardsFromMemory) {
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
