package com.gempukku.stccg.effectappender;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.ReturnCardsToHandEffect;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.ValueSource;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.effectappender.resolver.CardResolver;
import com.gempukku.stccg.effectappender.resolver.ValueResolver;
import com.gempukku.stccg.filters.Filter;
import com.gempukku.stccg.filters.Filters;
import org.json.simple.JSONObject;

import java.util.Collection;

public class ReturnToHand implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        environment.validateAllowedFields(effectObject, "filter", "count", "player");

        final String filter = environment.getString(effectObject.get("filter"), "filter");
        final String player = environment.getString(effectObject.get("player"), "player", "you");
        final ValueSource valueSource = ValueResolver.resolveEvaluator(effectObject.get("count"), 1, environment);

        MultiEffectAppender result = new MultiEffectAppender();

        result.addEffectAppender(
                CardResolver.resolveCards(filter,
                        actionContext -> (Filter) (game, physicalCard) -> game.getModifiersQuerying().canBeReturnedToHand(physicalCard, actionContext.getSource()),
                        valueSource, "_temp", player, "Choose cards to return to hand", environment));
        result.addEffectAppender(
                new DefaultDelayedAppender() {
                    @Override
                    protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext context) {
                        final Collection<? extends PhysicalCard> cardsFromMemory = context.getCardsFromMemory("_temp");
                        return new ReturnCardsToHandEffect(context.getGame(), context.getSource(), Filters.in(cardsFromMemory));
                    }
                });

        return result;
    }

}
