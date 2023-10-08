package com.gempukku.stccg.modifiers;

import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
import com.gempukku.stccg.effectappender.resolver.ValueResolver;
import com.gempukku.stccg.common.Keyword;
import com.gempukku.stccg.evaluator.Evaluator;
import com.gempukku.stccg.requirement.Requirement;
import org.json.simple.JSONObject;

public class AddKeyword implements ModifierSourceProducer {
    @Override
    public ModifierSource getModifierSource(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "filter", "requires", "keyword", "amount");

        final JSONObject[] conditionArray = FieldUtils.getObjectArray(object.get("requires"), "requires");
        final String filter = FieldUtils.getString(object.get("filter"), "filter");
        final String keywordString = FieldUtils.getString(object.get("keyword"), "keyword");

        final String[] keywordSplit = keywordString.split("\\+");
        Keyword keyword = FieldUtils.getEnum(Keyword.class, keywordSplit[0], "keyword");
        int value = 1;
        if (keywordSplit.length == 2)
            value = Integer.parseInt(keywordSplit[1]);

        final ValueSource amount = ValueResolver.resolveEvaluator(object.get("amount"), value, environment);

        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);
        final Requirement[] requirements = environment.getRequirementFactory().getRequirements(conditionArray, environment);

        return actionContext -> {
            final Evaluator evaluator = amount.getEvaluator(actionContext);
            return new KeywordModifier(actionContext.getSource(),
                    filterableSource.getFilterable(actionContext),
                    new RequirementCondition(requirements, actionContext), keyword, evaluator);
        };
    }
}
