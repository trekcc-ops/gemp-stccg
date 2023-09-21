package com.gempukku.lotro.effectappender;

import com.gempukku.lotro.cards.CardGenerationEnvironment;
import com.gempukku.lotro.actioncontext.DefaultActionContext;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.actions.CostToEffectAction;
import com.gempukku.lotro.effects.Effect;
import com.gempukku.lotro.effects.UnrespondableEffect;
import org.json.simple.JSONObject;

public class ReversePlayerOrder implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) {
        return new DelayedAppender<>() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, DefaultActionContext actionContext) {
                return new UnrespondableEffect() {
                    @Override
                    protected void doPlayEffect(DefaultGame game) {
                        actionContext.getGame().getGameState().getPlayerOrder().reversePlayerOrder();
                    }
                };
            }
        };
    }
}
