package com.gempukku.stccg.effectappender;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.discard.DiscardCardsFromZoneEffect;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.PlayerSource;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.effectappender.resolver.CardResolver;
import com.gempukku.stccg.effectappender.resolver.ValueResolver;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class DiscardCardsFromDrawDeck implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JsonNode effectObject, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {
        environment.validateAllowedFields(effectObject, "count", "filter", "memorize", "player", "deck");

        final String filter =
                environment.getString(effectObject, "filter", "choose(any)");
        final String memorize =
                environment.getString(effectObject, "memorize", "_temp");
        final PlayerSource choicePlayerSource =
                environment.getPlayerSource(effectObject, "player", true);
        final PlayerSource targetPlayerSource =
                environment.getPlayerSource(effectObject, "deck", true);

        MultiEffectAppender result = new MultiEffectAppender();

        result.addEffectAppender(
                CardResolver.resolveCardsInZone(filter, null,
                        ValueResolver.resolveEvaluator(effectObject.get("count"), 1, environment),
                        memorize, choicePlayerSource, targetPlayerSource, "Choose cards to discard",
                        environment, Zone.DRAW_DECK));
        result.addEffectAppender(
                new DefaultDelayedAppender() {
                    @Override
                    protected List<Effect> createEffects(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                        final Collection<? extends PhysicalCard> cardsToDiscard = actionContext.getCardsFromMemory(memorize);
                        List<Effect> result = new LinkedList<>();
                        for (PhysicalCard physicalCard : cardsToDiscard) {
                            result.add(new DiscardCardsFromZoneEffect(
                                    actionContext.getGame(), action.getActionSource(), Zone.DRAW_DECK, physicalCard));
                        }

                        return result;
                    }
                });

        return result;
    }

}


