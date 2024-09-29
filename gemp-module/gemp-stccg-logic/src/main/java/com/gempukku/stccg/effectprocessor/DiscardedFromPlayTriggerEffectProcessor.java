package com.gempukku.stccg.effectprocessor;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.actions.sources.ActionSource;
import com.gempukku.stccg.actions.sources.OptionalTriggerActionSource;
import com.gempukku.stccg.actions.sources.RequiredTriggerActionSource;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.common.filterable.RequiredType;

public class DiscardedFromPlayTriggerEffectProcessor implements EffectProcessor {
    @Override
    public void processEffect(JsonNode node, CardBlueprint blueprint, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {
        environment.validateAllowedFields(node, "optional", "requires", "cost", "effect");

        RequiredType requiredType = environment.getBoolean(node, "optional", false) ?
                RequiredType.OPTIONAL : RequiredType.REQUIRED;

        ActionSource triggerActionSource = requiredType == RequiredType.OPTIONAL ?
                new OptionalTriggerActionSource() : new RequiredTriggerActionSource();
        triggerActionSource.processRequirementsCostsAndEffects(node, environment);
        blueprint.setDiscardedFromPlayTrigger(requiredType, triggerActionSource);
    }
}
