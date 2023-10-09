package com.gempukku.stccg.effectappender;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.cards.CardGenerationEnvironment;
import com.gempukku.stccg.cards.DefaultActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
import com.gempukku.stccg.effects.Effect;
import com.gempukku.stccg.effects.choose.ChooseOpponentEffect;
import com.gempukku.stccg.game.DefaultGame;
import org.json.simple.JSONObject;

public class ChooseOpponent implements EffectAppenderProducer {
    @Override
    public EffectAppender<DefaultGame> createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "memorize");

        final String memorize = FieldUtils.getString(effectObject.get("memorize"), "memorize");

        return new DelayedAppender<>() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, DefaultActionContext<DefaultGame> actionContext) {
                return new ChooseOpponentEffect(actionContext.getPerformingPlayer()) {
                    @Override
                    protected void opponentChosen(String opponentId) {
                        actionContext.setValueToMemory(memorize, opponentId);
                    }
                };
            }
        };
    }

}
