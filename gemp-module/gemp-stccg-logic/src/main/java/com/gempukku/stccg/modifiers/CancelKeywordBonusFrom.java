package com.gempukku.stccg.modifiers;

import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.common.filterable.lotr.Keyword;
import com.gempukku.stccg.requirement.Requirement;
import org.json.simple.JSONObject;

public class CancelKeywordBonusFrom implements ModifierSourceProducer {
    @Override
    public ModifierSource getModifierSource(JSONObject object, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        environment.validateAllowedFields(object, "filter", "from", "requires", "keyword");

        final String filter = environment.getString(object.get("filter"), "filter");
        final String from = environment.getString(object.get("from"), "from");

        Keyword keyword = environment.getEnum(Keyword.class, object.get("keyword"), "keyword");

        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter);
        final FilterableSource fromFilterableSource = environment.getFilterFactory().generateFilter(from);
        final Requirement[] requirements = environment.getRequirementsFromJSON(object);

        return actionContext -> new CancelKeywordBonusTargetModifier(actionContext.getSource(), keyword,
                new RequirementCondition(requirements, actionContext),
                filterableSource.getFilterable(actionContext),
                fromFilterableSource.getFilterable(actionContext));
    }
}
