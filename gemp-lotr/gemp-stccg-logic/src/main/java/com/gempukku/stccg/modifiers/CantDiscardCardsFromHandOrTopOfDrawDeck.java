package com.gempukku.stccg.modifiers;

import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.requirement.Requirement;
import org.json.simple.JSONObject;

public class CantDiscardCardsFromHandOrTopOfDrawDeck implements ModifierSourceProducer {
    @Override
    public ModifierSource getModifierSource(JSONObject object, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        environment.validateAllowedFields(object, "requires", "filter");

        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(environment.getString(object.get("filter"), "filter"));
        final Requirement[] requirements = environment.getRequirementsFromJSON(object);

        return actionContext ->
                new CantDiscardCardsFromHandOrTopOfDeckModifier(actionContext.getSource(),
                        new RequirementCondition(requirements, actionContext),
                        actionContext.getPerformingPlayerId(),
                        filterableSource.getFilterable(actionContext));
    }
}
