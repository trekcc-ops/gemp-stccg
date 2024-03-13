package com.gempukku.stccg.effectappender;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.choose.ChooseTribblePowerEffect;
import org.json.simple.JSONObject;

public class ChooseTribblePower implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        environment.validateAllowedFields(effectObject, "memorize");

        final String memorize = environment.getString(effectObject.get("memorize"), "memorize");

        return new TribblesDelayedAppender() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext context) {
                return new ChooseTribblePowerEffect(context) {
                    @Override
                    protected void powerChosen(String playerId) {
                        context.setValueToMemory(memorize, playerId);
                    }
                };
            }
        };
    }

}
