package com.gempukku.stccg.effectappender;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.decisions.MultipleChoiceAwaitingDecision;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.PlayOutDecisionEffect;
import org.json.simple.JSONObject;

public class ChooseAKeyword implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        environment.validateAllowedFields(effectObject, "memorize", "keywords");

        final String memorize = environment.getString(effectObject.get("memorize"), "memorize");
        final String keywords = environment.getString(effectObject.get("keywords"), "keywords");

        return new DefaultDelayedAppender() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext context) {
                return new PlayOutDecisionEffect(
                        context.getGame(), context.getPerformingPlayerId(),
                        new MultipleChoiceAwaitingDecision("Choose a keyword",
                                keywords.split(",")) {
                            @Override
                            protected void validDecisionMade(int index, String result) {
                                context.setValueToMemory(memorize, result.toUpperCase().replace(' ', '_').replace('-', '_'));
                                context.getGame().sendMessage(context.getPerformingPlayerId() + " has chosen " + result);
                            }
                        });
            }
        };
    }
}
