package com.gempukku.stccg.modifiers;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.common.filterable.Keyword;
import com.gempukku.stccg.requirement.Requirement;

public class CancelKeywordBonusFrom implements ModifierSourceProducer {
    @Override
    public ModifierSource getModifierSource(JsonNode object, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        environment.validateAllowedFields(object, "filter", "from", "requires", "keyword");

        Keyword keyword = environment.getEnum(Keyword.class, object.get("keyword").textValue(), "keyword");

        final FilterableSource filterableSource =
                environment.getFilterFactory().generateFilter(object.get("filter").textValue());
        final FilterableSource fromFilterableSource =
                environment.getFilterFactory().generateFilter(object.get("from").textValue());
        final Requirement[] requirements = environment.getRequirementsFromJSON(object);

        return actionContext -> new CancelKeywordBonusTargetModifier(actionContext.getSource(), keyword,
                new RequirementCondition(requirements, actionContext),
                filterableSource.getFilterable(actionContext),
                fromFilterableSource.getFilterable(actionContext));
    }
}
