package com.gempukku.lotro.effectappender;

import com.gempukku.lotro.actions.CostToEffectAction;
import com.gempukku.lotro.cards.CardGenerationEnvironment;
import com.gempukku.lotro.actioncontext.DefaultActionContext;
import com.gempukku.lotro.cards.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.ModifierSource;
import com.gempukku.lotro.fieldprocessor.FieldUtils;
import com.gempukku.lotro.effectappender.resolver.TimeResolver;
import com.gempukku.lotro.effects.AddUntilModifierEffect;
import com.gempukku.lotro.effects.Effect;
import com.gempukku.lotro.modifiers.Modifier;
import org.json.simple.JSONObject;

public class AddModifier implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "modifier", "until");

        final JSONObject modifierObj = (JSONObject) effectObject.get("modifier");
        final TimeResolver.Time until = TimeResolver.resolveTime(effectObject.get("until"), "end(current)");

        ModifierSource modifierSource = environment.getModifierSourceFactory().getModifier(modifierObj, environment);

        return new DelayedAppender<>() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, DefaultActionContext actionContext) {
                final Modifier modifier = modifierSource.getModifier(actionContext);
                return new AddUntilModifierEffect(modifier, until);
            }
        };
    }
}
