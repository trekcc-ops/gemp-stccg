package com.gempukku.lotro.cards.build.field.effect.appender;

import com.gempukku.lotro.actions.lotronly.CostToEffectAction;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.DefaultActionContext;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.PlayerSource;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.PlayerResolver;
import com.gempukku.lotro.cards.lotronly.LotroPhysicalCard;
import com.gempukku.lotro.effects.Effect;
import com.gempukku.lotro.effects.UnrespondableEffect;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.game.TribblesGame;
import com.gempukku.lotro.gamestate.TribblesGameState;
import org.json.simple.JSONObject;

import java.util.LinkedList;
import java.util.List;

public class ShufflePlayPileIntoDrawDeck implements EffectAppenderProducer {
    @Override
    public EffectAppender<TribblesGame> createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "player");

        String player = FieldUtils.getString(effectObject.get("player"), "player", "you");
        final PlayerSource playerSource = PlayerResolver.resolvePlayer(player);

        return new DelayedAppender<>() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action,
                                          DefaultActionContext<TribblesGame> actionContext) {
                final String pileOwner = playerSource.getPlayer(actionContext);
                return new UnrespondableEffect() {
                    @Override
                    protected void doPlayEffect(DefaultGame game) {
                        TribblesGameState gameState = actionContext.getGame().getGameState();
                        List<LotroPhysicalCard> playPile = new LinkedList<>(gameState.getPlayPile(pileOwner));
                        gameState.removeCardsFromZone(actionContext.getPerformingPlayer(), playPile);
                        for (LotroPhysicalCard physicalCard : playPile) {
                            gameState.putCardOnBottomOfDeck(physicalCard);
                        }
                        gameState.shuffleDeck(pileOwner);
                    }
                };
            }
        };
    }
}