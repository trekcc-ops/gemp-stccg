package com.gempukku.stccg.modifiers;

import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.requirement.Requirement;
import com.gempukku.stccg.rules.lotronly.LotroPlayUtils;
import com.gempukku.stccg.actions.Action;
import org.json.simple.JSONObject;


import java.util.Collections;
import java.util.List;

public class CanPlayStackedCards implements ModifierSourceProducer {
    @Override
    public ModifierSource getModifierSource(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "filter", "on", "requires");

        final String filter = FieldUtils.getString(object.get("filter"), "filter");
        final String onFilter = FieldUtils.getString(object.get("on"), "on");
        final JSONObject[] conditionArray = FieldUtils.getObjectArray(object.get("requires"), "requires");

        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);
        final FilterableSource onFilterableSource = environment.getFilterFactory().generateFilter(onFilter, environment);
        final Requirement[] requirements = environment.getRequirementFactory().getRequirements(conditionArray, environment);

        return actionContext -> new AbstractModifier(actionContext.getSource(), null,
                Filters.and(filterableSource.getFilterable(actionContext), Filters.stackedOn(onFilterableSource.getFilterable(actionContext))),
                new RequirementCondition(requirements, actionContext), ModifierEffect.EXTRA_ACTION_MODIFIER) {
            @Override
            public List<? extends Action> getExtraPhaseActionFromStacked(DefaultGame game, PhysicalCard card) {
                if (LotroPlayUtils.checkPlayRequirements(game, card, Filters.any, 0, 0, false, false, false))
                    return Collections.singletonList(
                            LotroPlayUtils.getPlayCardAction(game, card, 0, Filters.any, false));
                return null;
            }
        };
    }
}
