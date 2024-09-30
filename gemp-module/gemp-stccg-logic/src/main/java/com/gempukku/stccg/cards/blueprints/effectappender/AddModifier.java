package com.gempukku.stccg.cards.blueprints.effectappender;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.blueprints.resolver.TimeResolver;
import com.gempukku.stccg.actions.turn.AddUntilModifierEffect;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.modifiers.Modifier;

public class AddModifier implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JsonNode node, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {
        environment.validateAllowedFields(node, "modifier", "until");

        final TimeResolver.Time until = TimeResolver.resolveTime(node.get("until"), "end(current)");
        ModifierSource modifierSource = environment.getModifier(node.get("modifier"));

        return new DefaultDelayedAppender() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext context) {
                final Modifier modifier = modifierSource.getModifier(context);
                return new AddUntilModifierEffect(context.getGame(), modifier, until);
            }
        };
    }
}
