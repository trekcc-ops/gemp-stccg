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
import com.gempukku.stccg.filters.Filter;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import org.json.simple.JSONObject;

import java.util.LinkedList;
import java.util.List;

public class GetCardsFromTopOfDeck implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "filter", "memorize");

        final String filter = FieldUtils.getString(effectObject.get("filter"), "filter");
        final String memorize = FieldUtils.getString(effectObject.get("memorize"), "memorize");

        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);

        return new DelayedAppender<>() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, DefaultActionContext actionContext) {
                final Filterable filterable = filterableSource.getFilterable(actionContext);
                final Filter acceptFilter = Filters.and(filterable);
                return new UnrespondableEffect() {
                    @Override
                    protected void doPlayEffect(DefaultGame game) {
                        final List<? extends PhysicalCard> deck = game.getGameState().getDrawDeck(actionContext.getPerformingPlayer());
                        List<PhysicalCard> result = new LinkedList<>();
                        for (PhysicalCard physicalCard : deck) {
                            if (acceptFilter.accepts(game, physicalCard))
                                result.add(physicalCard);
                            else
                                break;
                        }

                        actionContext.setCardMemory(memorize, result);
                    }
                };
            }
        };
    }
}
