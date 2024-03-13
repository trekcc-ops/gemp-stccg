package com.gempukku.stccg.effectappender;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.effectappender.resolver.PlayerResolver;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.choose.ChoosePlayerExceptEffect;
import org.json.simple.JSONObject;

public class ChoosePlayerExcept implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        environment.validateAllowedFields(effectObject, "memorize", "exclude");

        final String memorize = environment.getString(effectObject.get("memorize"), "memorize");
        final String excludePlayer = environment.getString(effectObject.get("exclude"), "exclude", "you");
        final PlayerSource excludePlayerSource = PlayerResolver.resolvePlayer(excludePlayer);

        return new DefaultDelayedAppender() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext context) {
                final String excludedPlayer = excludePlayerSource.getPlayerId(context);
                return new ChoosePlayerExceptEffect(context, excludedPlayer) {
                    @Override
                    protected void playerChosen(String playerId) {
                        context.setValueToMemory(memorize, playerId);
                    }
                };
            }
        };
    }

}
