package com.gempukku.stccg.effectappender;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.TransferPermanentEffect;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
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

public class Transfer implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        environment.validateAllowedFields(effectObject, "filter", "where", "checkTarget", "memorizeTransferred", "memorizeTarget");

        final String filter = environment.getString(effectObject.get("filter"), "filter");
        final String where = environment.getString(effectObject.get("where"), "where");
        final boolean checkTarget = environment.getBoolean(effectObject.get("checkTarget"), "checkTarget", false);
        final String memorizeTransferred = environment.getString(effectObject.get("memorizeTransferred"), "memorizeTransferred", "_temp1");
        final String memorizeTarget = environment.getString(effectObject.get("memorizeTarget"), "memorizeTarget", "_temp2");

        MultiEffectAppender result = new MultiEffectAppender();

        result.addEffectAppender(
                CardResolver.resolveCard(filter, memorizeTransferred, "you", "Choose card to transfer", environment));
        result.addEffectAppender(
                CardResolver.resolveCards(where,
                        actionContext -> (Filter) (game, physicalCard) -> {
                            final Collection<? extends PhysicalCard> transferCard = actionContext.getCardsFromMemory(memorizeTransferred);
                            if (transferCard.isEmpty())
                                return false;
                            final PhysicalCard transferredCard = transferCard.iterator().next();
                            // Can't be transferred to card it's already attached to
                            if (transferredCard.getAttachedTo() == physicalCard)
                                return false;
                            // Optionally check target against original target filter
                            if (checkTarget && !transferredCard.getFullValidTargetFilter().accepts(game, physicalCard))
                                return false;

                            return actionContext.getGame().getModifiersQuerying().canHaveTransferredOn(game, transferredCard, physicalCard);
                        }, actionContext -> Filters.any,
                        ValueResolver.resolveEvaluator(1, environment), memorizeTarget, "you", "Choose cards to transfer to", environment));
        result.addEffectAppender(
                new DefaultDelayedAppender() {
                    @Override
                    protected List<? extends Effect> createEffects(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                        final Collection<? extends PhysicalCard> transferCard = actionContext.getCardsFromMemory(memorizeTransferred);
                        if (transferCard.isEmpty())
                            return null;

                        final Collection<? extends PhysicalCard> transferredToCard = actionContext.getCardsFromMemory(memorizeTarget);
                        if (transferredToCard.isEmpty())
                            return null;

                        return Collections.singletonList(new TransferPermanentEffect(actionContext.getGame(), transferCard.iterator().next(), transferredToCard.iterator().next()));
                    }
                });

        return result;
    }
}
