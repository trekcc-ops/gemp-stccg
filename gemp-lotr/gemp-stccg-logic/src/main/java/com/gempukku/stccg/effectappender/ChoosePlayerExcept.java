package com.gempukku.stccg.effectappender;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.cards.CardGenerationEnvironment;
import com.gempukku.stccg.cards.DefaultActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.PlayerSource;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
import com.gempukku.stccg.effectappender.resolver.PlayerResolver;
import com.gempukku.stccg.effects.Effect;
import com.gempukku.stccg.effects.choose.ChoosePlayerExceptEffect;
import org.json.simple.JSONObject;

public class ChoosePlayerExcept implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "memorize", "exclude");

        final String memorize = FieldUtils.getString(effectObject.get("memorize"), "memorize");
        final String excludePlayer = FieldUtils.getString(effectObject.get("exclude"), "exclude", "you");
        final PlayerSource excludePlayerSource = PlayerResolver.resolvePlayer(excludePlayer);

        return new DelayedAppender<>() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, DefaultActionContext actionContext) {
                final String excludedPlayer = excludePlayerSource.getPlayer(actionContext);
                return new ChoosePlayerExceptEffect(actionContext.getPerformingPlayer(), excludedPlayer) {
                    @Override
                    protected void playerChosen(String playerId) {
                        actionContext.setValueToMemory(memorize, playerId);
                    }
                };
            }
        };
    }

}
