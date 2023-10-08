package com.gempukku.stccg.effectappender;

import com.gempukku.stccg.cards.DefaultActionContext;
import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
import com.gempukku.stccg.effectappender.resolver.PlayerResolver;
import com.gempukku.stccg.effectappender.resolver.ValueResolver;
import com.gempukku.stccg.effects.Effect;
import com.gempukku.stccg.effects.RevealTopCardsOfDrawDeckEffect;
import com.gempukku.stccg.game.DefaultGame;
import org.json.simple.JSONObject;

import java.util.List;

public class RevealTopCardsOfDrawDeck implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "deck", "count", "memorize");

        final String deck = FieldUtils.getString(effectObject.get("deck"), "deck", "you");
        final ValueSource valueSource = ValueResolver.resolveEvaluator(effectObject.get("count"), 1, environment);
        final String memorize = FieldUtils.getString(effectObject.get("memorize"), "memorize");

        final PlayerSource playerSource = PlayerResolver.resolvePlayer(deck);

        return new DelayedAppender<>() {
            @Override
            public boolean isPlayableInFull(DefaultActionContext<DefaultGame> actionContext) {
                final String deckId = playerSource.getPlayer(actionContext);
                final int count = valueSource.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), null);

                return actionContext.getGame().getGameState().getDrawDeck(deckId).size() >= count;
            }

            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, DefaultActionContext actionContext) {
                final String deckId = playerSource.getPlayer(actionContext);
                final int count = valueSource.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), null);

                return new RevealTopCardsOfDrawDeckEffect(actionContext.getSource(), deckId, count) {
                    @Override
                    protected void cardsRevealed(List<PhysicalCard> revealedCards) {
                        if (memorize != null)
                            actionContext.setCardMemory(memorize, revealedCards);
                    }
                };
            }
        };
    }
}
