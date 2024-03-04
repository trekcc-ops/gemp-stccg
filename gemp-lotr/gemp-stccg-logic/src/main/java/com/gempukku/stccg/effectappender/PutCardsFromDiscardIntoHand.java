package com.gempukku.stccg.effectappender;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.PutCardFromZoneIntoHandEffect;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.ValueSource;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.effectappender.resolver.CardResolver;
import com.gempukku.stccg.effectappender.resolver.ValueResolver;
import org.json.simple.JSONObject;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class PutCardsFromDiscardIntoHand implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        environment.validateAllowedFields(effectObject, "count", "filter", "player", "discard", "memorize");

        final String player = environment.getString(effectObject.get("player"), "player", "you");
        final String discard = environment.getString(effectObject.get("discard"), "discard", player);
        final ValueSource valueSource = ValueResolver.resolveEvaluator(effectObject.get("count"), 1, environment);
        final String filter = environment.getString(effectObject.get("filter"), "filter", "choose(any)");
        final String memorize = environment.getString(effectObject.get("memorize"), "memorize", "_temp");

        MultiEffectAppender result = new MultiEffectAppender();

        result.addEffectAppender(
                CardResolver.resolveCardsInDiscard(filter, valueSource, memorize, player, discard, "Choose cards from discard", environment));
        result.addEffectAppender(
                new DefaultDelayedAppender() {
                    @Override
                    protected List<? extends Effect> createEffects(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                        final Collection<? extends PhysicalCard> cards = actionContext.getCardsFromMemory(memorize);
                        List<Effect> result = new LinkedList<>();
                        for (PhysicalCard card : cards) {
                            result.add(
                                    new PutCardFromZoneIntoHandEffect(actionContext.getGame(), card, Zone.DISCARD));
                        }

                        return result;
                    }
                });

        return result;

    }
}
