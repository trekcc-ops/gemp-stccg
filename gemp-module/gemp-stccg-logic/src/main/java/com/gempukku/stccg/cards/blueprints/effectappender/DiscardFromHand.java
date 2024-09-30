package com.gempukku.stccg.cards.blueprints.effectappender;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.discard.DiscardCardsFromZoneEffect;
import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.blueprints.ValueSource;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.cards.blueprints.resolver.CardResolver;
import com.gempukku.stccg.cards.blueprints.resolver.PlayerResolver;
import com.gempukku.stccg.cards.blueprints.resolver.ValueResolver;
import com.gempukku.stccg.game.DefaultGame;

import java.util.Collection;

public class DiscardFromHand implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JsonNode effectObject, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {
        environment.validateAllowedFields(effectObject, "forced", "count", "filter", "memorize", "hand", "player");

        final boolean forced = environment.getBoolean(effectObject, "forced");
        final String filter = environment.getString(effectObject, "filter", "choose(any)");
        final String memorize = environment.getString(effectObject, "memorize", "_temp");

        final PlayerSource handSource =
                PlayerResolver.resolvePlayer(environment.getString(effectObject, "hand", "you"));
        final PlayerSource playerSource =
                PlayerResolver.resolvePlayer(environment.getString(effectObject, "player", "you"));
        final ValueSource countSource = ValueResolver.resolveEvaluator(effectObject.get("count"), 1, environment);

        MultiEffectAppender result = new MultiEffectAppender();

        result.addEffectAppender(
                CardResolver.resolveCardsInHand(filter, countSource, memorize, playerSource, handSource,
                        "Choose cards from hand to discard", true, environment));
        result.addEffectAppender(
                new DefaultDelayedAppender() {
                    @Override
                    protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext context) {
                        final Collection<PhysicalCard> cardsToDiscard = context.getCardsFromMemory(memorize);
                        return new DiscardCardsFromZoneEffect(context, Zone.HAND, handSource.getPlayerId(context), cardsToDiscard, forced);
                    }

                    @Override
                    public boolean isPlayableInFull(ActionContext actionContext) {
                        final DefaultGame game = actionContext.getGame();

                        final String handPlayer = handSource.getPlayerId(actionContext);
                        final String choosingPlayer = playerSource.getPlayerId(actionContext);
                        if (!handPlayer.equals(choosingPlayer)
                                && !game.getModifiersQuerying().canLookOrRevealCardsInHand(game, handPlayer, choosingPlayer))
                            return false;

                        return (!forced || game.getModifiersQuerying().canDiscardCardsFromHand(handPlayer, actionContext.getSource()));
                    }
                });

        return result;
    }

}
