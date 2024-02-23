package com.gempukku.stccg.effectappender;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.common.filterable.EndOfPile;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.effectappender.resolver.PlayerResolver;
import com.gempukku.stccg.effectappender.resolver.ValueResolver;
import com.gempukku.stccg.effects.defaulteffect.DiscardCardsFromEndOfCardPileEffect;
import com.gempukku.stccg.effects.Effect;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
import com.gempukku.stccg.game.TribblesGame;
import org.json.simple.JSONObject;

import java.util.Collection;

public class DiscardTopCardFromPlayPile implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "deck", "count", "memorize");

        final String deck = FieldUtils.getString(effectObject.get("deck"), "deck", "you");
        final String memorize = FieldUtils.getString(effectObject.get("memorize"), "memorize");
        final ValueSource countSource = ValueResolver.resolveEvaluator(effectObject.get("count"), 1, environment);

        final PlayerSource playerSource = PlayerResolver.resolvePlayer(deck);

        return new TribblesDelayedAppender() {
            @Override
            public boolean isPlayableInFull(ActionContext actionContext) {
                final int count = countSource.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), null);
                return actionContext.getGameState().getZoneCards(
                        playerSource.getPlayerId(actionContext), Zone.PLAY_PILE).size() >= count;
            }

            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                final String deckId = playerSource.getPlayerId(actionContext);
                final int count = countSource.getEvaluator(actionContext).evaluateExpression(
                        actionContext.getGame(), null);

                return new DiscardCardsFromEndOfCardPileEffect(
                        actionContext.getGame(), actionContext.getSource(), Zone.PLAY_PILE, EndOfPile.TOP,
                        deckId, count, true) {
                    @Override
                    protected void cardsDiscardedCallback(Collection<PhysicalCard> cards) {
                        if (memorize != null)
                            actionContext.setCardMemory(memorize, cards);
                    }
                };
            }
        };
    }

}


