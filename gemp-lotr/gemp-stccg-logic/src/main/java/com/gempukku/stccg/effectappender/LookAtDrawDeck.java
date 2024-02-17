package com.gempukku.stccg.effectappender;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.effectappender.resolver.PlayerResolver;
import com.gempukku.stccg.effects.Effect;
import com.gempukku.stccg.effects.defaulteffect.LookAtTopCardOfADeckEffect;
import com.gempukku.stccg.effects.defaulteffect.unrespondable.ShuffleDeckEffect;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
import org.json.simple.JSONObject;

import java.util.List;

public class LookAtDrawDeck implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "deck", "memorize");

        final String deck = FieldUtils.getString(effectObject.get("deck"), "deck", "you");
        final String memorize = FieldUtils.getString(effectObject.get("memorize"), "memorize");

        final PlayerSource playerSource = PlayerResolver.resolvePlayer(deck);

        MultiEffectAppender result = new MultiEffectAppender();

        result.addEffectAppender(new DefaultDelayedAppender() {

            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                final String deckId = playerSource.getPlayerId(actionContext);
                final int count = actionContext.getGame().getGameState().getDrawDeck(deckId).size();

                return new LookAtTopCardOfADeckEffect(actionContext, count, deckId) {
                    @Override
                    protected void cardsLookedAt(List<? extends PhysicalCard> cards) {
                        if (memorize != null)
                            actionContext.setCardMemory(memorize, cards);
                    }
                };
            }
        });
        result.addEffectAppender(new DefaultDelayedAppender() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                return new ShuffleDeckEffect(actionContext.getGame(), playerSource.getPlayerId(actionContext));
            }
        });

        return result;
    }
}
