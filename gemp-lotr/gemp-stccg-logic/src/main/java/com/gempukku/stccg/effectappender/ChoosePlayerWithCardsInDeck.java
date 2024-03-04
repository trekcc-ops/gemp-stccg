package com.gempukku.stccg.effectappender;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.choose.ChoosePlayerWithCardsInDeckEffect;
import org.json.simple.JSONObject;

public class ChoosePlayerWithCardsInDeck implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {
        environment.validateAllowedFields(effectObject, "memorize");

        final String memorize = environment.getString(effectObject.get("memorize"), "memorize");

        return new DefaultDelayedAppender() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext context) {
                return new ChoosePlayerWithCardsInDeckEffect(context) {
                    @Override
                    protected void playerChosen(String playerId) {
                        context.setValueToMemory(memorize, playerId);
                    }
                };
            }
        };
    }

}
