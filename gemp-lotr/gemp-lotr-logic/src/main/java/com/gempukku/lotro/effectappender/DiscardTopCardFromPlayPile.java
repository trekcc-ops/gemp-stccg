package com.gempukku.lotro.effectappender;

import com.gempukku.lotro.actioncontext.DefaultActionContext;
import com.gempukku.lotro.actions.CostToEffectAction;
import com.gempukku.lotro.cards.*;
import com.gempukku.lotro.fieldprocessor.FieldUtils;
import com.gempukku.lotro.effectappender.resolver.PlayerResolver;
import com.gempukku.lotro.effectappender.resolver.ValueResolver;
import com.gempukku.lotro.cards.LotroPhysicalCard;
import com.gempukku.lotro.effects.DiscardTopCardFromPlayPileEffect;
import com.gempukku.lotro.effects.Effect;
import com.gempukku.lotro.game.TribblesGame;
import org.json.simple.JSONObject;

import java.util.Collection;

public class DiscardTopCardFromPlayPile implements EffectAppenderProducer {
    @Override
    public EffectAppender<TribblesGame> createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "deck", "count", "memorize");

        final String deck = FieldUtils.getString(effectObject.get("deck"), "deck", "you");
        final String memorize = FieldUtils.getString(effectObject.get("memorize"), "memorize");
        final ValueSource countSource = ValueResolver.resolveEvaluator(effectObject.get("count"), 1, environment);

        final PlayerSource playerSource = PlayerResolver.resolvePlayer(deck);

        return new DelayedAppender<>() {
            @Override
            public boolean isPlayableInFull(DefaultActionContext<TribblesGame> actionContext) {
                final String deckId = playerSource.getPlayer(actionContext);
                final int count = countSource.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), null);

                final TribblesGame game = actionContext.getGame();
                return game.getGameState().getPlayPile(deckId).size() >= count;
            }

            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, DefaultActionContext<TribblesGame> actionContext) {
                final String deckId = playerSource.getPlayer(actionContext);
                final int count = countSource.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), null);

                return new DiscardTopCardFromPlayPileEffect(actionContext.getSource(), deckId, count) {
                    @Override
                    protected void cardsDiscardedCallback(Collection<LotroPhysicalCard> cards) {
                        if (memorize != null)
                            actionContext.setCardMemory(memorize, cards);
                    }
                };
            }
        };
    }

}


