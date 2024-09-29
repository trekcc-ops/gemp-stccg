package com.gempukku.stccg.modifiers;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.effectappender.resolver.ValueResolver;
import com.gempukku.stccg.common.filterable.Keyword;
import com.gempukku.stccg.requirement.Requirement;

public class AddKeyword implements ModifierSourceProducer {
    @Override
    public ModifierSource getModifierSource(JsonNode node, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {
        environment.validateAllowedFields(node, "filter", "requires", "keyword", "amount");

        final FilterableSource filterableSource = environment.getFilterable(node);
        final String[] keywordSplit = node.get("keyword").textValue().split("\\+");

        Keyword keyword = environment.getEnum(Keyword.class, keywordSplit[0], "keyword");
        int value = 1;
        if (keywordSplit.length == 2)
            value = Integer.parseInt(keywordSplit[1]);

        final ValueSource amount = ValueResolver.resolveEvaluator(node.get("amount"), value, environment);
        final Requirement[] requirements = environment.getRequirementsFromJSON(node);

        return actionContext -> new KeywordModifier(actionContext,
                filterableSource.getFilterable(actionContext),
                new RequirementCondition(requirements, actionContext), keyword, amount);
    }
}
