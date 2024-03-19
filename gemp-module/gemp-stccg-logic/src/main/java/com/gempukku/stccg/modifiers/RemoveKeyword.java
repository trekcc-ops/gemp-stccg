package com.gempukku.stccg.modifiers;

import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.common.filterable.lotr.Keyword;
import com.gempukku.stccg.requirement.Requirement;
import org.json.simple.JSONObject;

public class RemoveKeyword implements ModifierSourceProducer {
    @Override
    public ModifierSource getModifierSource(JSONObject object, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        environment.validateAllowedFields(object, "filter", "requires", "keyword");

        Keyword keyword = environment.getEnum(Keyword.class, object.get("keyword"), "keyword");
        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(environment.getString(object.get("filter"), "filter"));
        final Requirement[] requirements = environment.getRequirementsFromJSON(object);

        return actionContext -> new RemoveKeywordModifier(actionContext.getSource(),
                filterableSource.getFilterable(actionContext),
                new RequirementCondition(requirements, actionContext), keyword);
    }
}
