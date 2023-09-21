package com.gempukku.lotro.effectappender;

import com.gempukku.lotro.actions.CostToEffectAction;
import com.gempukku.lotro.cards.CardGenerationEnvironment;
import com.gempukku.lotro.actioncontext.DefaultActionContext;
import com.gempukku.lotro.cards.InvalidCardDefinitionException;
import com.gempukku.lotro.fieldprocessor.FieldUtils;
import com.gempukku.lotro.effects.Effect;
import com.gempukku.lotro.effects.choose.ChooseOpponentEffect;
import com.gempukku.lotro.game.DefaultGame;
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
