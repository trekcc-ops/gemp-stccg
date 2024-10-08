package com.gempukku.stccg.cards.blueprints.effect;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.revealcards.RevealTopCardsOfDrawDeckEffect;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.PlayerSource;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.blueprints.ValueSource;
import com.gempukku.stccg.cards.blueprints.resolver.ValueResolver;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;

import java.util.List;

public class RevealTopCardsOfDrawDeck implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JsonNode effectObject, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {
        environment.validateAllowedFields(effectObject, "player", "count", "memorize");

        final ValueSource valueSource =
                ValueResolver.resolveEvaluator(effectObject.get("count"), 1, environment);
        final String memorize = effectObject.get("memorize").textValue();

        final PlayerSource playerSource = environment.getPlayerSource(effectObject, "player", true);

        return new DefaultDelayedAppender() {
            @Override
            public boolean isPlayableInFull(ActionContext actionContext) {
                final String deckId = playerSource.getPlayerId(actionContext);
                final int count = valueSource.evaluateExpression(actionContext, null);

                return actionContext.getGameState().getDrawDeck(deckId).size() >= count;
            }

            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext context) {
                final String deckId = playerSource.getPlayerId(context);
                final int count = valueSource.evaluateExpression(context, null);

                return new RevealTopCardsOfDrawDeckEffect(context, deckId, count) {
                    @Override
                    protected void cardsRevealed(List<PhysicalCard> revealedCards) {
                        if (memorize != null)
                            context.setCardMemory(memorize, revealedCards);
                    }
                };
            }
        };
    }
}