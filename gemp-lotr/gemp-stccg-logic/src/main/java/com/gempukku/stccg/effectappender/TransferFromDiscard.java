package com.gempukku.stccg.effectappender;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.TransferPermanentNotFromPlayEffect;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.CardBlueprintFactory;
import com.gempukku.stccg.cards.ConstantValueSource;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.effectappender.resolver.CardResolver;
import com.gempukku.stccg.effectappender.resolver.ValueResolver;
import com.gempukku.stccg.filters.Filter;
import com.gempukku.stccg.filters.Filters;
import org.json.simple.JSONObject;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class TransferFromDiscard implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        environment.validateAllowedFields(effectObject, "filter", "where");

        final String filter = environment.getString(effectObject.get("filter"), "filter");
        final String where = environment.getString(effectObject.get("where"), "where");

        MultiEffectAppender result = new MultiEffectAppender();

        result.addEffectAppender(
                CardResolver.resolveCardsInDiscard(filter, new ConstantValueSource(1), "_temp1",
                        "you", "Choose card to transfer", environment));
        result.addEffectAppender(
                CardResolver.resolveCards(where,
                        actionContext -> (Filter) (game, physicalCard) -> {
                            final Collection<? extends PhysicalCard> transferCard = actionContext.getCardsFromMemory("_temp1");
                            if (transferCard.isEmpty())
                                return false;
                            final PhysicalCard transferredCard = transferCard.iterator().next();
                            // Can't be transferred to card it's already attached to
                            if (transferredCard.getAttachedTo() == physicalCard)
                                return false;
                            return actionContext.getGame().getModifiersQuerying().canHaveTransferredOn(game, transferredCard, physicalCard);
                        }, actionContext -> Filters.any,
                        ValueResolver.resolveEvaluator(1, environment), "_temp2", "you", "Choose cards to transfer to", environment));
        result.addEffectAppender(
                new DefaultDelayedAppender() {
                    @Override
                    protected List<? extends Effect> createEffects(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                        final Collection<? extends PhysicalCard> transferCard = actionContext.getCardsFromMemory("_temp1");
                        if (transferCard.isEmpty())
                            return null;

                        final Collection<? extends PhysicalCard> transferredToCard = actionContext.getCardsFromMemory("_temp2");
                        if (transferredToCard.isEmpty())
                            return null;

                        return Collections.singletonList(new TransferPermanentNotFromPlayEffect(actionContext.getGame(), transferCard.iterator().next(), transferredToCard.iterator().next()));
                    }
                });

        return result;
    }
}
