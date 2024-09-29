package com.gempukku.stccg.modifiers;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.effectappender.resolver.ValueResolver;
import com.gempukku.stccg.evaluator.Evaluator;
import com.gempukku.stccg.modifiers.attributes.StrengthModifier;
import com.gempukku.stccg.requirement.Requirement;

public class ModifyStrength implements ModifierSourceProducer {
    @Override
    public ModifierSource getModifierSource(JsonNode object, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        environment.validateAllowedFields(object, "filter", "requires", "amount");

        final ValueSource valueSource = ValueResolver.resolveEvaluator(object.get("amount"), environment);
        final FilterableSource filterableSource = environment.getFilterable(object);
        final Requirement[] requirements = environment.getRequirementsFromJSON(object);

        return (actionContext) -> {
                    final Evaluator evaluator = valueSource.getEvaluator(actionContext);
                    return new StrengthModifier(actionContext,
                            filterableSource.getFilterable(actionContext),
                            new RequirementCondition(requirements, actionContext), evaluator);
        };
    }
}
