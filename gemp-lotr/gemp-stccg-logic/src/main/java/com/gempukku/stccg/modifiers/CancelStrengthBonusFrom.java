package com.gempukku.stccg.modifiers;

import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.requirement.Requirement;
import org.json.simple.JSONObject;

public class CancelStrengthBonusFrom implements ModifierSourceProducer {
    @Override
    public ModifierSource getModifierSource(JSONObject object, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        environment.validateAllowedFields(object, "from", "requires");

        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(environment.getString(object.get("from"), "from"));
        final Requirement[] requirements = environment.getRequirementsFromJSON(object);

        return actionContext -> new CancelStrengthBonusSourceModifier(actionContext.getSource(),
                new RequirementCondition(requirements, actionContext),
                filterableSource.getFilterable(actionContext));
    }
}
