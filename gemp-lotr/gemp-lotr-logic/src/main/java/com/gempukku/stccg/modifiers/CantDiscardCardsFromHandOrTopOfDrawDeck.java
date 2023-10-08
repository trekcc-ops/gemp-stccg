package com.gempukku.stccg.modifiers;

import com.gempukku.stccg.cards.CardGenerationEnvironment;
import com.gempukku.stccg.cards.FilterableSource;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.ModifierSource;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.requirement.Requirement;
import org.json.simple.JSONObject;

public class CantDiscardCardsFromHandOrTopOfDrawDeck implements ModifierSourceProducer {
    @Override
    public ModifierSource getModifierSource(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "requires", "filter");

        final String filter = FieldUtils.getString(object.get("filter"), "filter");
        final JSONObject[] conditionArray = FieldUtils.getObjectArray(object.get("requires"), "requires");

        final FilterableSource<DefaultGame> filterableSource = environment.getFilterFactory().generateFilter(filter, environment);
        final Requirement<DefaultGame>[] requirements = environment.getRequirementFactory().getRequirements(conditionArray, environment);

        return actionContext ->
                new CantDiscardCardsFromHandOrTopOfDeckModifier(actionContext.getSource(),
                        new RequirementCondition(requirements, actionContext),
                        actionContext.getPerformingPlayer(),
                        filterableSource.getFilterable(actionContext));
    }
}
