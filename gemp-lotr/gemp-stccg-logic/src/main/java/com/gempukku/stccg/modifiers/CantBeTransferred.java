package com.gempukku.stccg.modifiers;

import com.gempukku.stccg.cards.CardGenerationEnvironment;
import com.gempukku.stccg.cards.FilterableSource;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.ModifierSource;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.requirement.Requirement;
import org.json.simple.JSONObject;

public class CantBeTransferred implements ModifierSourceProducer {
    @Override
    public ModifierSource getModifierSource(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "filter", "requires");

        final JSONObject[] conditionArray = FieldUtils.getObjectArray(object.get("requires"), "requires");
        final String filter = FieldUtils.getString(object.get("filter"), "filter");

        final FilterableSource filterableSource =
                environment.getFilterFactory().generateFilter(filter, environment);
        final Requirement[] requirements =
                environment.getRequirementFactory().getRequirements(conditionArray, environment);

        return (actionContext) -> new CantBeTransferredModifier(actionContext.getSource(), filterableSource.getFilterable(actionContext), new RequirementCondition(requirements, actionContext));
    }
}
