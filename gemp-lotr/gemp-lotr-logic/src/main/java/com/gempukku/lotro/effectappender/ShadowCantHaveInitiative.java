package com.gempukku.lotro.effectappender;

import com.gempukku.lotro.actions.CostToEffectAction;
import com.gempukku.lotro.cards.CardGenerationEnvironment;
import com.gempukku.lotro.actioncontext.DefaultActionContext;
import com.gempukku.lotro.cards.InvalidCardDefinitionException;
import com.gempukku.lotro.fieldprocessor.FieldUtils;
import com.gempukku.lotro.effectappender.resolver.TimeResolver;
import com.gempukku.lotro.effects.AddUntilModifierEffect;
import com.gempukku.lotro.effects.Effect;
import com.gempukku.lotro.modifiers.lotronly.ShadowCantHaveInitiativeModifier;
import org.json.simple.JSONObject;

public class ShadowCantHaveInitiative implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "until");

        final TimeResolver.Time until = TimeResolver.resolveTime(effectObject.get("until"), "end(current)");

        return new DelayedAppender<>() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, DefaultActionContext actionContext) {
                return new AddUntilModifierEffect(
                        new ShadowCantHaveInitiativeModifier(actionContext.getSource(), null), until);
            }
        };
    }
}
