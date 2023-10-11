package com.gempukku.stccg.effectappender;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.effectappender.resolver.PlayerResolver;
import com.gempukku.stccg.effects.Effect;
import com.gempukku.stccg.effects.defaulteffect.UnrespondableEffect;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
import com.gempukku.stccg.gamestate.TribblesGameState;
import org.json.simple.JSONObject;

import java.util.LinkedList;
import java.util.List;

public class ShufflePlayPileIntoDrawDeck implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "player");

        String player = FieldUtils.getString(effectObject.get("player"), "player", "you");
        final PlayerSource playerSource = PlayerResolver.resolvePlayer(player);

        return new TribblesDelayedAppender() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action,
                                          TribblesActionContext actionContext) {
                final String pileOwner = playerSource.getPlayer(actionContext);
                return new UnrespondableEffect() {
                    @Override
                    protected void doPlayEffect() {
                        TribblesGameState gameState = actionContext.getGame().getGameState();
                        List<PhysicalCard> playPile = new LinkedList<>(gameState.getPlayPile(pileOwner));
                        gameState.removeCardsFromZone(actionContext.getPerformingPlayer(), playPile);
                        for (PhysicalCard physicalCard : playPile) {
                            gameState.putCardOnBottomOfDeck(physicalCard);
                        }
                        gameState.shuffleDeck(pileOwner);
                    }
                };
            }
        };
    }
}