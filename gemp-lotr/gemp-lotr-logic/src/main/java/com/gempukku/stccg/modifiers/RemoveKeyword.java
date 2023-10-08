package com.gempukku.stccg.modifiers;

import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
import com.gempukku.stccg.common.Keyword;
import com.gempukku.stccg.requirement.Requirement;
import org.json.simple.JSONObject;

public class RemoveKeyword implements ModifierSourceProducer {
    @Override
    public ModifierSource getModifierSource(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "filter", "requires", "keyword");

        final JSONObject[] conditionArray = FieldUtils.getObjectArray(object.get("requires"), "requires");
        final String filter = FieldUtils.getString(object.get("filter"), "filter");

        Keyword keyword = FieldUtils.getEnum(Keyword.class, object.get("keyword"), "keyword");

        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);
        final Requirement[] requirements = environment.getRequirementFactory().getRequirements(conditionArray, environment);

        return actionContext -> new RemoveKeywordModifier(actionContext.getSource(),
                filterableSource.getFilterable(actionContext),
                new RequirementCondition(requirements, actionContext), keyword);
    }
}
