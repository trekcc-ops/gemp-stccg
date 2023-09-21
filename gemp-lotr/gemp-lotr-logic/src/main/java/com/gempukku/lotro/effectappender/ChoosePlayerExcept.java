package com.gempukku.lotro.effectappender;

import com.gempukku.lotro.actions.CostToEffectAction;
import com.gempukku.lotro.cards.CardGenerationEnvironment;
import com.gempukku.lotro.actioncontext.DefaultActionContext;
import com.gempukku.lotro.cards.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.PlayerSource;
import com.gempukku.lotro.fieldprocessor.FieldUtils;
import com.gempukku.lotro.effectappender.resolver.PlayerResolver;
import com.gempukku.lotro.effects.Effect;
import com.gempukku.lotro.effects.choose.ChoosePlayerExceptEffect;
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
