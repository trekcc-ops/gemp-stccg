package com.gempukku.stccg.modifiers;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.FilterableSource;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.ModifierSource;
import com.gempukku.stccg.cards.ValueSource;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.common.filterable.CardIcon;
import com.gempukku.stccg.effectappender.resolver.ValueResolver;
import com.gempukku.stccg.requirement.Requirement;

public class AddIcon implements ModifierSourceProducer {
    @Override
    public ModifierSource getModifierSource(JsonNode node, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {
        environment.validateAllowedFields(node, "filter", "requires", "icon", "amount");

        final String filter = node.get("filter").textValue();
        final String iconString = node.get("icon").textValue();

        final String[] iconSplit = iconString.split("\\+");
        CardIcon icon = environment.getEnum(CardIcon.class, iconSplit[0], "icon");
        int value = 1;
        if (iconSplit.length == 2)
            value = Integer.parseInt(iconSplit[1]);

        final ValueSource amount = ValueResolver.resolveEvaluator(node.get("amount"), value, environment);

        final FilterableSource filterableSource = environment.getFilterFactory().parseSTCCGFilter(filter);
        final Requirement[] requirements = environment.getRequirementsFromJSON(node);

        return actionContext -> new GainIconModifier(actionContext,
                filterableSource.getFilterable(actionContext),
                new RequirementCondition(requirements, actionContext), icon, amount);
    }
}
