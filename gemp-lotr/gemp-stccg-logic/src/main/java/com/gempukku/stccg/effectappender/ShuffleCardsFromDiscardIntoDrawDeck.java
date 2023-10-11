package com.gempukku.stccg.effectappender;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.effectappender.resolver.CardResolver;
import com.gempukku.stccg.effectappender.resolver.PlayerResolver;
import com.gempukku.stccg.effectappender.resolver.ValueResolver;
import com.gempukku.stccg.effects.Effect;
import com.gempukku.stccg.effects.defaulteffect.ShuffleCardsIntoDrawDeckEffect;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
import org.json.simple.JSONObject;

import java.util.Collection;

public class ShuffleCardsFromDiscardIntoDrawDeck implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "filter", "count", "player");

        final String filter = FieldUtils.getString(effectObject.get("filter"), "filter", "choose(any)");
            // Added the next 2 lines
        final String player = FieldUtils.getString(effectObject.get("player"), "player", "you");
        final PlayerSource playerSource = PlayerResolver.resolvePlayer(player);

        final ValueSource valueSource = ValueResolver.resolveEvaluator(effectObject.get("count"), 1, environment);

        MultiEffectAppender result = new MultiEffectAppender();

        result.addEffectAppender(
                CardResolver.resolveCardsInDiscard(filter, valueSource, "_temp", player, "Choose cards to shuffle in", environment));
        result.addEffectAppender(
                new DefaultDelayedAppender() {
                    @Override
                    protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                        final String recyclePlayer = playerSource.getPlayer(actionContext);
                        final Collection<PhysicalCard> cardsInDiscard = actionContext.getCardsFromMemory("_temp");
                        return new ShuffleCardsIntoDrawDeckEffect(actionContext.getGame(), actionContext.getSource(), Zone.DISCARD, recyclePlayer, cardsInDiscard);
                    }
                });
        return result;
    }

}