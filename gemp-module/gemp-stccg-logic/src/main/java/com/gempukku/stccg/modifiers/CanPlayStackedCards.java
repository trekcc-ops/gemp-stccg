package com.gempukku.stccg.modifiers;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.requirement.Requirement;

import java.util.Collections;
import java.util.List;

public class CanPlayStackedCards implements ModifierSourceProducer {
    @Override
    public ModifierSource getModifierSource(JsonNode node, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {
        environment.validateAllowedFields(node, "filter", "on", "requires");

        final FilterableSource filterableSource = environment.getFilterable(node);
        final FilterableSource onFilterableSource =
                environment.getFilterFactory().generateFilter(node.get("on").textValue());
        final Requirement[] requirements = environment.getRequirementsFromJSON(node);

        return actionContext -> new AbstractModifier(actionContext.getSource(), null,
                Filters.and(filterableSource.getFilterable(actionContext),
                        Filters.stackedOn(onFilterableSource.getFilterable(actionContext))),
                new RequirementCondition(requirements, actionContext), ModifierEffect.EXTRA_ACTION_MODIFIER) {
            @Override
            public List<? extends Action> getExtraPhaseActionFromStacked(DefaultGame game, PhysicalCard card) {
                if (card.canBePlayed())
                    return Collections.singletonList(
                            card.getPlayCardAction(Filters.any, false));
                return null;
            }
        };
    }
}
