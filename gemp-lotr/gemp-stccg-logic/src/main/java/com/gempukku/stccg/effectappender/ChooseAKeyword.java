package com.gempukku.stccg.effectappender;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.CardGenerationEnvironment;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.decisions.MultipleChoiceAwaitingDecision;
import com.gempukku.stccg.effects.Effect;
import com.gempukku.stccg.effects.PlayOutDecisionEffect;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
import org.json.simple.JSONObject;

public class ChooseAKeyword implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "memorize", "keywords");

        final String memorize = FieldUtils.getString(effectObject.get("memorize"), "memorize");
        final String keywords = FieldUtils.getString(effectObject.get("keywords"), "keywords");

        return new DefaultDelayedAppender() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                return new PlayOutDecisionEffect(
                        actionContext.getGame(), actionContext.getPerformingPlayer(),
                        new MultipleChoiceAwaitingDecision(1, "Choose a keyword",
                                keywords.split(",")) {
                            @Override
                            protected void validDecisionMade(int index, String result) {
                                actionContext.setValueToMemory(memorize, result.toUpperCase().replace(' ', '_').replace('-', '_'));
                                actionContext.getGameState().sendMessage(actionContext.getPerformingPlayer() + " has chosen " + result);
                            }
                        });
            }
        };
    }
}
