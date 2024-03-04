package com.gempukku.stccg.modifiers;

import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.effectappender.resolver.ValueResolver;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.requirement.Requirement;
import org.json.simple.JSONObject;

public class ModifyPlayOnCost implements ModifierSourceProducer {
    @Override
    public ModifierSource getModifierSource(JSONObject object, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {
        environment.validateAllowedFields(object, "requires", "filter", "on", "amount");

        final Requirement[] conditions = environment.getRequirementsFromJSON(object);
        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(environment.getString(object.get("filter"), "filter"));
        final FilterableSource onFilterableSource = environment.getFilterFactory().generateFilter(environment.getString(object.get("on"), "on"));
        final ValueSource amountSource = ValueResolver.resolveEvaluator(object.get("amount"), environment);

        return actionContext -> {
            final Filterable filterable = filterableSource.getFilterable(actionContext);
            final Filterable onFilterable = onFilterableSource.getFilterable(actionContext);
            final RequirementCondition requirementCondition = new RequirementCondition(conditions, actionContext);
            return new AbstractModifier(actionContext.getSource(), "Cost to play on is modified", filterable,
                    requirementCondition, ModifierEffect.TWILIGHT_COST_MODIFIER) {
                @Override
                public int getTwilightCostModifier(PhysicalCard physicalCard, PhysicalCard target,
                                                   boolean ignoreRoamingPenalty) {
                    if (target != null && Filters.and(onFilterable).accepts(_game, target))
                        return amountSource.evaluateExpression(actionContext, null);
                    return 0;
                }
            };
        };
    }
}
