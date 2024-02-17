package com.gempukku.stccg.effectappender;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.effectappender.resolver.PlayerResolver;
import com.gempukku.stccg.effects.Effect;
import com.gempukku.stccg.effects.defaulteffect.UnrespondableEffect;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
import com.gempukku.stccg.game.DefaultGame;
import org.json.simple.JSONObject;

import java.util.LinkedList;
import java.util.List;

public class ShuffleHandIntoDrawDeck implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "player");

        String player = FieldUtils.getString(effectObject.get("player"), "player", "you");
        final PlayerSource playerSource = PlayerResolver.resolvePlayer(player);

        return new DefaultDelayedAppender() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                DefaultGame game = actionContext.getGame();
                final String handPlayer = playerSource.getPlayerId(actionContext);
                return new UnrespondableEffect() {
                    @Override
                    protected void doPlayEffect() {
                        List<PhysicalCard> hand = new LinkedList<>(game.getGameState().getHand(handPlayer));
                        game.getGameState().removeCardsFromZone(actionContext.getPerformingPlayer(), hand);
                        for (PhysicalCard physicalCard : hand) {
                            game.getGameState().putCardOnBottomOfDeck(physicalCard);
                        }
                        game.getGameState().shuffleDeck(handPlayer);
                    }
                };
            }
        };
    }
}
