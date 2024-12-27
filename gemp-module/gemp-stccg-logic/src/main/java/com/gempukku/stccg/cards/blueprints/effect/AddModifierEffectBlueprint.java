package com.gempukku.stccg.cards.blueprints.effect;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.modifiers.AddUntilModifierAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.blueprints.BlueprintUtils;
import com.gempukku.stccg.cards.blueprints.resolver.TimeResolver;
import com.gempukku.stccg.modifiers.Modifier;

import java.util.List;

public class AddModifierEffectBlueprint extends DelayedEffectBlueprint {

    private final TimeResolver.Time _until;
    private final ModifierSource _modifierSource;

    public AddModifierEffectBlueprint(JsonNode node) throws InvalidCardDefinitionException {
        BlueprintUtils.validateAllowedFields(node, "modifier", "until");
        _until = TimeResolver.resolveTime(node.get("until"), "end(current)");
        _modifierSource = BlueprintUtils.getModifier(node.get("modifier"));
    }

    @Override
    protected List<Action> createActions(Action parentAction, ActionContext context) {
        final Modifier modifier = _modifierSource.getModifier(context);
        Action action = new AddUntilModifierAction(context.getSource(), modifier, _until);
        return List.of(action);
    }
}