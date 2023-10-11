package com.gempukku.stccg.effectappender;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.CardGenerationEnvironment;
import com.gempukku.stccg.effects.Effect;
import com.gempukku.stccg.effects.defaulteffect.UnrespondableEffect;
import org.json.simple.JSONObject;

public class ReversePlayerOrder implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) {
        return new DefaultDelayedAppender() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                return new UnrespondableEffect() {
                    @Override
                    protected void doPlayEffect() {
                        actionContext.getGame().getGameState().getPlayerOrder().reversePlayerOrder();
                    }
                };
            }
        };
    }
}
