package com.gempukku.stccg.modifiers;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.common.filterable.Keyword;
import com.gempukku.stccg.requirement.Requirement;

public class RemoveKeyword implements ModifierSourceProducer {
    @Override
    public ModifierSource getModifierSource(JsonNode object, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {
        environment.validateAllowedFields(object, "filter", "requires", "keyword");

        Keyword keyword = environment.getEnum(Keyword.class, object.get("keyword").textValue(), "keyword");
        final FilterableSource filterableSource = environment.getFilterable(object);
        final Requirement[] requirements = environment.getRequirementsFromJSON(object);

        return actionContext -> new RemoveKeywordModifier(actionContext.getSource(),
                filterableSource.getFilterable(actionContext),
                new RequirementCondition(requirements, actionContext), keyword);
    }
}
