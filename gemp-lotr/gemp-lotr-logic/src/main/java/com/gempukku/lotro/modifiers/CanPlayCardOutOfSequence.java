package com.gempukku.lotro.modifiers;

import com.gempukku.lotro.cards.CardGenerationEnvironment;
import com.gempukku.lotro.cards.FilterableSource;
import com.gempukku.lotro.cards.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.ModifierSource;
import com.gempukku.lotro.fieldprocessor.FieldUtils;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.requirement.Requirement;
import org.json.simple.JSONObject;

public class CanPlayCardOutOfSequence implements ModifierSourceProducer {
    @Override
    public ModifierSource getModifierSource(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "filter", "requires");

        final String filter = FieldUtils.getString(object.get("filter"), "filter");
        final JSONObject[] conditionArray = FieldUtils.getObjectArray(object.get("requires"), "requires");

        final Requirement<DefaultGame>[] requirements =
                environment.getRequirementFactory().getRequirements(conditionArray, environment);
        final FilterableSource<DefaultGame> filterableSource =
                environment.getFilterFactory().generateFilter(filter, environment);

        return actionContext ->
                new CanPlayCardOutOfSequenceModifier(actionContext.getSource(),
                        new RequirementCondition(requirements, actionContext),
                        filterableSource.getFilterable(actionContext));
    }
}
