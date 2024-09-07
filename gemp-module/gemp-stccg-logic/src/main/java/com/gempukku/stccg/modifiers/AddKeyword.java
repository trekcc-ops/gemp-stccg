package com.gempukku.stccg.modifiers;

import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.effectappender.resolver.ValueResolver;
import com.gempukku.stccg.common.filterable.lotr.Keyword;
import com.gempukku.stccg.requirement.Requirement;
import org.json.simple.JSONObject;

public class AddKeyword implements ModifierSourceProducer {
    @Override
    public ModifierSource getModifierSource(JSONObject object, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {
        environment.validateAllowedFields(object, "filter", "requires", "keyword", "amount");

        final FilterableSource filterableSource = environment.getFilterable(object);
        final String[] keywordSplit = environment.getString(object.get("keyword"), "keyword").split("\\+");

        Keyword keyword = environment.getEnum(Keyword.class, keywordSplit[0], "keyword");
        int value = 1;
        if (keywordSplit.length == 2)
            value = Integer.parseInt(keywordSplit[1]);

        final ValueSource amount = ValueResolver.resolveEvaluator(object.get("amount"), value, environment);
        final Requirement[] requirements = environment.getRequirementsFromJSON(object);

        return actionContext -> new KeywordModifier(actionContext,
                filterableSource.getFilterable(actionContext),
                new RequirementCondition(requirements, actionContext), keyword, amount);
    }
}
