package com.gempukku.stccg.modifiers;

import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.common.filterable.Icon1E;
import com.gempukku.stccg.effectappender.resolver.ValueResolver;
import com.gempukku.stccg.requirement.Requirement;
import org.json.simple.JSONObject;

public class AddIcon implements ModifierSourceProducer {
    @Override
    public ModifierSource getModifierSource(JSONObject object, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {
        environment.validateAllowedFields(object, "filter", "requires", "icon", "amount");

        final String filter = environment.getString(object.get("filter"), "filter");
        final String iconString = environment.getString(object.get("icon"), "icon");

        final String[] iconSplit = iconString.split("\\+");
        Icon1E icon = environment.getEnum(Icon1E.class, iconSplit[0], "icon");
        int value = 1;
        if (iconSplit.length == 2)
            value = Integer.parseInt(iconSplit[1]);

        final ValueSource amount = ValueResolver.resolveEvaluator(object.get("amount"), value, environment);

        final FilterableSource filterableSource = environment.getFilterFactory().parseSTCCGFilter(filter);
        final Requirement[] requirements =
                environment.getRequirementsFromJSON(object);

        return actionContext -> new GainIconModifier(actionContext,
                filterableSource.getFilterable(actionContext),
                new RequirementCondition(requirements, actionContext), icon, amount);
    }
}
