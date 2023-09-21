package com.gempukku.lotro.effectappender;

import com.gempukku.lotro.actions.CostToEffectAction;
import com.gempukku.lotro.cards.CardGenerationEnvironment;
import com.gempukku.lotro.actioncontext.DefaultActionContext;
import com.gempukku.lotro.cards.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.ValueSource;
import com.gempukku.lotro.fieldprocessor.FieldUtils;
import com.gempukku.lotro.effectappender.resolver.CardResolver;
import com.gempukku.lotro.effectappender.resolver.ValueResolver;
import com.gempukku.lotro.cards.LotroPhysicalCard;
import com.gempukku.lotro.effects.Effect;
import com.gempukku.lotro.effects.ReturnCardsToHandEffect;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.filters.Filters;
import org.json.simple.JSONObject;

import java.util.Collection;

public class ReturnToHand implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "filter", "count", "player");

        final String filter = FieldUtils.getString(effectObject.get("filter"), "filter");
        final String player = FieldUtils.getString(effectObject.get("player"), "player", "you");
        final ValueSource valueSource = ValueResolver.resolveEvaluator(effectObject.get("count"), 1, environment);

        MultiEffectAppender result = new MultiEffectAppender();

        result.addEffectAppender(
                CardResolver.resolveCards(filter,
                        actionContext -> (Filter) (game, physicalCard) -> game.getModifiersQuerying().canBeReturnedToHand(game, physicalCard, actionContext.getSource()),
                        valueSource, "_temp", player, "Choose cards to return to hand", environment));
        result.addEffectAppender(
                new DelayedAppender() {
                    @Override
                    protected Effect createEffect(boolean cost, CostToEffectAction action, DefaultActionContext actionContext) {
                        final Collection<? extends LotroPhysicalCard> cardsFromMemory = actionContext.getCardsFromMemory("_temp");
                        return new ReturnCardsToHandEffect(actionContext.getSource(), Filters.in(cardsFromMemory));
                    }
                });

        return result;
    }

}
