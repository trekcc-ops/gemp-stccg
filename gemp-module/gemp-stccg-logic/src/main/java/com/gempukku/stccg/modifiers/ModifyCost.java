package com.gempukku.stccg.modifiers;

import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.effectappender.resolver.ValueResolver;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.requirement.Requirement;
import org.json.simple.JSONObject;

public class ModifyCost implements ModifierSourceProducer {
    @Override
    public ModifierSource getModifierSource(JSONObject object, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        environment.validateAllowedFields(object, "requires", "filter", "amount");

        final Requirement[] conditions = environment.getRequirementsFromJSON(object);
        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(environment.getString(object.get("filter"), "filter"));
        final ValueSource amountSource = ValueResolver.resolveEvaluator(object.get("amount"), environment);

        return actionContext -> {
                    final Filterable filterable = filterableSource.getFilterable(actionContext);
                    final RequirementCondition requirementCondition = new RequirementCondition(conditions, actionContext);
                    return new CostModifier(actionContext, filterable, requirementCondition, amountSource);
        };
    }
}
