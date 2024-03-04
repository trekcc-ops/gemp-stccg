package com.gempukku.stccg.effectappender;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.choose.ChooseOpponentEffect;
import org.json.simple.JSONObject;

public class ChooseOpponent implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        environment.validateAllowedFields(effectObject, "memorize");

        final String memorize = environment.getString(effectObject.get("memorize"), "memorize");

        return new DefaultDelayedAppender() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext context) {
                return new ChooseOpponentEffect(context) {
                    @Override
                    protected void playerChosen(String opponentId) {
                        context.setValueToMemory(memorize, opponentId);
                    }
                };
            }
        };
    }

}
