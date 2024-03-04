package com.gempukku.stccg.modifiers;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.requirement.Requirement;
import org.json.simple.JSONObject;

import java.util.Collections;
import java.util.List;

public class CanPlayStackedCards implements ModifierSourceProducer {
    @Override
    public ModifierSource getModifierSource(JSONObject object, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {
        environment.validateAllowedFields(object, "filter", "on", "requires");

        final FilterableSource filterableSource = environment.getFilterable(object);
        final FilterableSource onFilterableSource = environment.getFilterFactory().generateFilter(environment.getString(object.get("on"), "on"));
        final Requirement[] requirements = environment.getRequirementsFromJSON(object);

        return actionContext -> new AbstractModifier(actionContext.getSource(), null,
                Filters.and(filterableSource.getFilterable(actionContext),
                        Filters.stackedOn(onFilterableSource.getFilterable(actionContext))),
                new RequirementCondition(requirements, actionContext), ModifierEffect.EXTRA_ACTION_MODIFIER) {
            @Override
            public List<? extends Action> getExtraPhaseActionFromStacked(DefaultGame game, PhysicalCard card) {
                if (card.canBePlayed())
                    return Collections.singletonList(
                            card.getPlayCardAction(0, Filters.any, false));
                return null;
            }
        };
    }
}
