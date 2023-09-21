package com.gempukku.lotro.effectappender;

import com.gempukku.lotro.actions.CostToEffectAction;
import com.gempukku.lotro.cards.CardGenerationEnvironment;
import com.gempukku.lotro.actioncontext.DefaultActionContext;
import com.gempukku.lotro.cards.InvalidCardDefinitionException;
import com.gempukku.lotro.fieldprocessor.FieldUtils;
import com.gempukku.lotro.effects.AddUntilEndOfPhaseModifierEffect;
import com.gempukku.lotro.effects.Effect;
import com.gempukku.lotro.modifiers.PlayersCantPlayPhaseEventsOrPhaseSpecialAbilitiesModifier;
import org.json.simple.JSONObject;

public class EndPhase implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject);

        return new DelayedAppender<>() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, DefaultActionContext actionContext) {
                return new AddUntilEndOfPhaseModifierEffect(
                        new PlayersCantPlayPhaseEventsOrPhaseSpecialAbilitiesModifier(actionContext.getSource(), actionContext.getGame().getGameState().getCurrentPhase()));
            }
        };
    }
}
