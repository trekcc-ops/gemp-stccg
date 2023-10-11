package com.gempukku.stccg.effectprocessor;

import com.gempukku.stccg.cards.BuiltCardBlueprint;
import com.gempukku.stccg.cards.CardGenerationEnvironment;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.actions.DefaultActionSource;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
import org.json.simple.JSONObject;

public class EventEffectProcessor implements EffectProcessor {
    @Override
    public void processEffect(JSONObject value, BuiltCardBlueprint blueprint, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(value, "cost", "effect", "requires", "requiresRanger");

        final boolean requiresRanger = FieldUtils.getBoolean(value.get("requiresRanger"), "requiresRanger", false);

        DefaultActionSource actionSource = new DefaultActionSource();
        actionSource.setRequiresRanger(requiresRanger);

        EffectUtils.processRequirementsCostsAndEffects(value, environment, actionSource);

        blueprint.setPlayEventAction(actionSource);
    }
}
