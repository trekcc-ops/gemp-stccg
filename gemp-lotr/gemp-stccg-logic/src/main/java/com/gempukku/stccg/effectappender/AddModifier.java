package com.gempukku.stccg.effectappender;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.effectappender.resolver.TimeResolver;
import com.gempukku.stccg.actions.turn.AddUntilModifierEffect;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.modifiers.Modifier;
import org.json.simple.JSONObject;

public class AddModifier implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        environment.validateAllowedFields(effectObject, "modifier", "until");

        final JSONObject modifierObj = (JSONObject) effectObject.get("modifier");
        final TimeResolver.Time until = TimeResolver.resolveTime(effectObject.get("until"), "end(current)");

        ModifierSource modifierSource = environment.getModifierSourceFactory().getModifier(modifierObj, environment);

        return new DefaultDelayedAppender() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext context) {
                final Modifier modifier = modifierSource.getModifier(context);
                return new AddUntilModifierEffect(context.getGame(), modifier, until);
            }
        };
    }
}
