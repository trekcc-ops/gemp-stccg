package com.gempukku.stccg.effectappender;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.PutCardFromZoneIntoHandEffect;
import com.gempukku.stccg.actions.ShuffleDeckEffect;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
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

public class PutCardsFromDeckIntoHand implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        environment.validateAllowedFields(effectObject, "count", "filter", "shuffle", "reveal");

        final ValueSource valueSource = ValueResolver.resolveEvaluator(effectObject.get("count"), 1, environment);
        final String filter = environment.getString(effectObject.get("filter"), "filter", "choose(any)");
        final boolean shuffle = environment.getBoolean(effectObject.get("shuffle"), "shuffle", true);
        final boolean reveal = environment.getBoolean(effectObject.get("reveal"), "reveal", true);

        MultiEffectAppender result = new MultiEffectAppender();

        result.addEffectAppender(
                CardResolver.resolveCardsInDeck(filter, valueSource, "_temp", "you", "Choose cards from deck", environment));
        result.addEffectAppender(
                new DefaultDelayedAppender() {
                    @Override
                    protected List<? extends Effect> createEffects(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                        final Collection<? extends PhysicalCard> cards = actionContext.getCardsFromMemory("_temp");
                        List<Effect> result = new LinkedList<>();
                        for (PhysicalCard card : cards) {
                            result.add(
                                    new PutCardFromZoneIntoHandEffect(actionContext.getGame(), card, Zone.DRAW_DECK, reveal));
                        }

                        return result;
                    }
                });
        if (shuffle)
            result.addEffectAppender(
                    new DefaultDelayedAppender() {
                @Override
                protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext context) {
                    return new ShuffleDeckEffect(context.getGame(), context.getPerformingPlayerId());
                }
            });

        return result;

    }
}
