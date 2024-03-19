package com.gempukku.stccg.modifiers;

import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.effectprocessor.EffectProcessor;
import com.gempukku.stccg.effectappender.resolver.ValueResolver;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.requirement.Requirement;
import org.json.simple.JSONObject;

public class ModifyOwnCost implements EffectProcessor {
    @Override
    public void processEffect(JSONObject value, CardBlueprint blueprint, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        environment.validateAllowedFields(value, "amount", "requires", "on");

        final ValueSource amountSource = ValueResolver.resolveEvaluator(value.get("amount"), environment);

        final FilterableSource onFilterableSource = environment.getFilterFactory().generateFilter(environment.getString(value.get("on"), "on", "any"));
        final Requirement[] requirements = environment.getRequirementsFromJSON(value);

        blueprint.appendTwilightCostModifier(
                (actionContext, target) -> {
                    for (Requirement requirement : requirements) {
                        if (!requirement.accepts(actionContext))
                            return 0;
                    }

                    if(target == null && onFilterableSource.getFilterable(actionContext) != Filters.any)
                        return 0;

                    if (target != null) {
                        if (!Filters.and(onFilterableSource.getFilterable(actionContext)).accepts(actionContext.getGame(), target))
                            return 0;
                    }

                    return amountSource.evaluateExpression(actionContext);
                });
    }
}
