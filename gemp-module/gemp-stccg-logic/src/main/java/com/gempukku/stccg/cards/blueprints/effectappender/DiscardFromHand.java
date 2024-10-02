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
        environment.validateAllowedFields(effectObject, "forced", "count", "filter", "memorize", "targetPlayer", "selectingPlayer");

        final boolean forced = environment.getBoolean(effectObject, "forced");
        final String memorize = environment.getString(effectObject, "memorize", "_temp");

        final PlayerSource targetPlayer =
                PlayerResolver.resolvePlayer(environment.getString(effectObject, "targetPlayer", "you"));
        final PlayerSource selectingPlayer =
                PlayerResolver.resolvePlayer(environment.getString(effectObject, "selectingPlayer", "you"));

        MultiEffectAppender result = new MultiEffectAppender();

        EffectAppender targetCardAppender = environment.buildTargetCardAppender(effectObject, selectingPlayer,
                targetPlayer, "Choose cards from hand to discard", Zone.HAND, memorize, true);
        result.addEffectAppender(targetCardAppender);

        result.addEffectAppender(
                new DefaultDelayedAppender() {
                    @Override
                    protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext context) {
                        final Collection<PhysicalCard> cardsToDiscard = context.getCardsFromMemory(memorize);
                        return new DiscardCardsFromZoneEffect(context, Zone.HAND, targetPlayer.getPlayerId(context), cardsToDiscard, forced);
                    }

                    @Override
                    public boolean isPlayableInFull(ActionContext actionContext) {
                        final DefaultGame game = actionContext.getGame();

                        final String handPlayer = targetPlayer.getPlayerId(actionContext);
                        final String choosingPlayer = selectingPlayer.getPlayerId(actionContext);
                        if (!handPlayer.equals(choosingPlayer)
                                && !game.getModifiersQuerying().canLookOrRevealCardsInHand(game, handPlayer, choosingPlayer))
                            return false;

                        return (!forced || game.getModifiersQuerying().canDiscardCardsFromHand(handPlayer, actionContext.getSource()));
                    }
                });

        return result;
    }

}
