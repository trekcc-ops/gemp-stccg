package com.gempukku.stccg.effectappender;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.UnrespondableEffect;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.CardBlueprintFactory;
import org.json.simple.JSONObject;

public class ReversePlayerOrder implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardBlueprintFactory environment) {
        return new DefaultDelayedAppender() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext context) {
                return new UnrespondableEffect() {
                    @Override
                    protected void doPlayEffect() {
                        context.getGameState().getPlayerOrder().reversePlayerOrder();
                    }
                };
            }
        };
    }
}
