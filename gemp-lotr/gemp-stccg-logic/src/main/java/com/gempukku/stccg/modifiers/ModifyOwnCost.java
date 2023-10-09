package com.gempukku.stccg.modifiers;

import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.effectprocessor.EffectProcessor;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
import com.gempukku.stccg.effectappender.resolver.ValueResolver;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.evaluator.Evaluator;
import com.gempukku.stccg.requirement.Requirement;
import org.json.simple.JSONObject;

public class ModifyOwnCost implements EffectProcessor {
    @Override
    public void processEffect(JSONObject value, BuiltLotroCardBlueprint blueprint, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(value, "amount", "requires", "on");

        final String onFilter = FieldUtils.getString(value.get("on"), "on", "any");
        final ValueSource amountSource = ValueResolver.resolveEvaluator(value.get("amount"), environment);
        final JSONObject[] conditionArray = FieldUtils.getObjectArray(value.get("requires"), "requires");

        final FilterableSource onFilterableSource = environment.getFilterFactory().generateFilter(onFilter, environment);
        final Requirement[] requirements = environment.getRequirementFactory().getRequirements(conditionArray, environment);

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

                    final Evaluator evaluator = amountSource.getEvaluator(actionContext);
                    return evaluator.evaluateExpression(actionContext.getGame(), actionContext.getSource());
                });
    }
}
