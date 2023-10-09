package com.gempukku.stccg.effectappender;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.cards.CardGenerationEnvironment;
import com.gempukku.stccg.cards.DefaultActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.ModifierSource;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
import com.gempukku.stccg.effectappender.resolver.TimeResolver;
import com.gempukku.stccg.effects.AddUntilModifierEffect;
import com.gempukku.stccg.effects.Effect;
import com.gempukku.stccg.modifiers.Modifier;
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
