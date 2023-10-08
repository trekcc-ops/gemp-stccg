package com.gempukku.stccg.modifiers;

import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.requirement.Requirement;
import org.json.simple.JSONObject;

public class CantBeDiscarded implements ModifierSourceProducer {
    @Override
    public ModifierSource getModifierSource(JSONObject object, CardGenerationEnvironment environment)
            throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "filter", "requires", "by");

        final JSONObject[] conditionArray = FieldUtils.getObjectArray(object.get("requires"), "requires");
        final String filter = FieldUtils.getString(object.get("filter"), "filter");
        final String byFilter = FieldUtils.getString(object.get("by"), "by", "any");

        final FilterableSource<DefaultGame> filterableSource =
                environment.getFilterFactory().generateFilter(filter, environment);
        final FilterableSource<DefaultGame> byFilterableSource =
                environment.getFilterFactory().generateFilter(byFilter, environment);
        final Requirement<DefaultGame>[] requirements =
                environment.getRequirementFactory().getRequirements(conditionArray, environment);

        return (actionContext) -> new CantDiscardFromPlayModifier(actionContext.getSource(),
                "Can't be discarded",
                new RequirementCondition(requirements, actionContext),
                filterableSource.getFilterable(actionContext),
                byFilterableSource.getFilterable(actionContext));
    }
}
