package com.gempukku.stccg.effectappender;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.ShuffleCardsIntoDrawDeckEffect;
import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.effectappender.resolver.CardResolver;
import com.gempukku.stccg.effectappender.resolver.PlayerResolver;
import com.gempukku.stccg.effectappender.resolver.ValueResolver;
import org.json.simple.JSONObject;

import java.util.Collection;

public class ShuffleCardsFromDiscardIntoDrawDeck implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        environment.validateAllowedFields(effectObject, "filter", "count", "player");

        final String filter = environment.getString(effectObject.get("filter"), "filter", "choose(any)");
            // Added the next 2 lines
        final String player = environment.getString(effectObject.get("player"), "player", "you");
        final PlayerSource playerSource = PlayerResolver.resolvePlayer(player);

        final ValueSource valueSource = ValueResolver.resolveEvaluator(effectObject.get("count"), 1, environment);

        MultiEffectAppender result = new MultiEffectAppender();

        result.addEffectAppender(
                CardResolver.resolveCardsInDiscard(filter, valueSource, "_temp", player, "Choose cards to shuffle in", environment));
        result.addEffectAppender(
                new DefaultDelayedAppender() {
                    @Override
                    protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext context) {
                        final String recyclePlayer = playerSource.getPlayerId(context);
                        final Collection<PhysicalCard> cardsInDiscard = context.getCardsFromMemory("_temp");
                        return new ShuffleCardsIntoDrawDeckEffect(context.getGame(), context.getSource(), Zone.DISCARD, recyclePlayer, cardsInDiscard);
                    }
                });
        return result;
    }

}