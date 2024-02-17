package com.gempukku.stccg.effectappender;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.effectappender.resolver.CardResolver;
import com.gempukku.stccg.effectappender.resolver.PlayerResolver;
import com.gempukku.stccg.effectappender.resolver.ValueResolver;
import com.gempukku.stccg.effects.defaulteffect.DiscardCardsFromZoneEffect;
import com.gempukku.stccg.effects.Effect;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
import com.gempukku.stccg.game.DefaultGame;
import org.json.simple.JSONObject;

import java.util.Collection;

public class DiscardFromHand implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "forced", "count", "filter", "memorize", "hand", "player");

        final String hand = FieldUtils.getString(effectObject.get("hand"), "hand", "you");
        final String player = FieldUtils.getString(effectObject.get("player"), "player", "you");
        final boolean forced = FieldUtils.getBoolean(effectObject.get("forced"), "forced");
        final String filter = FieldUtils.getString(effectObject.get("filter"), "filter", "choose(any)");
        final String memorize = FieldUtils.getString(effectObject.get("memorize"), "memorize", "_temp");

        final PlayerSource handSource = PlayerResolver.resolvePlayer(hand);
        final PlayerSource playerSource = PlayerResolver.resolvePlayer(player);
        final ValueSource countSource = ValueResolver.resolveEvaluator(effectObject.get("count"), 1, environment);

        MultiEffectAppender result = new MultiEffectAppender();

        result.addEffectAppender(
                CardResolver.resolveCardsInHand(filter, countSource, memorize, player, hand, "Choose cards from hand to discard", true, environment));
        result.addEffectAppender(
                new DefaultDelayedAppender() {
                    @Override
                    protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                        final Collection<PhysicalCard> cardsToDiscard = actionContext.getCardsFromMemory(memorize);
                        return new DiscardCardsFromZoneEffect(actionContext, Zone.HAND, handSource.getPlayerId(actionContext), cardsToDiscard, forced);
                    }

                    @Override
                    public boolean isPlayableInFull(ActionContext actionContext) {
                        final DefaultGame game = actionContext.getGame();

                        final String handPlayer = handSource.getPlayerId(actionContext);
                        final String choosingPlayer = playerSource.getPlayerId(actionContext);
                        if (!handPlayer.equals(choosingPlayer)
                                && !game.getModifiersQuerying().canLookOrRevealCardsInHand(game, handPlayer, choosingPlayer))
                            return false;

                        return (!forced || game.getModifiersQuerying().canDiscardCardsFromHand(game, handPlayer, actionContext.getSource()));
                    }
                });

        return result;
    }

}
