package com.gempukku.stccg.cards.blueprints.modifiersourceproducer;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.blueprints.FilterableSource;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.blueprints.effect.EffectAppender;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.modifiers.AbstractExtraPlayCostModifier;
import com.gempukku.stccg.modifiers.RequirementCondition;
import com.gempukku.stccg.cards.blueprints.requirement.Requirement;

import java.util.List;

public class ExtraCostToPlay implements ModifierSourceProducer {
    @Override
    public ModifierSource getModifierSource(JsonNode node, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {
        environment.validateAllowedFields(node, "requires", "cost", "filter");

        final FilterableSource filterableSource = environment.getFilterable(node);
        final Requirement[] requirements = environment.getRequirementsFromJSON(node);
        final List<EffectAppender> effectAppenders = environment.getEffectAppendersFromJSON(node.get("cost"));

        return (actionContext) -> {
            final Filterable filterable = filterableSource.getFilterable(actionContext);
            final RequirementCondition condition = new RequirementCondition(requirements, actionContext);

            return new AbstractExtraPlayCostModifier(actionContext.getSource(),
                    "Cost to play is modified", filterable, condition) {
                @Override
                public void appendExtraCosts(DefaultGame game, CostToEffectAction action, PhysicalCard card) {
                    for (EffectAppender effectAppender : effectAppenders)
                        effectAppender.appendEffect(true, action, actionContext);
                }

                @Override
                public boolean canPayExtraCostsToPlay(DefaultGame game, PhysicalCard card) {
                    for (EffectAppender effectAppender : effectAppenders) {
                        if (!effectAppender.isPlayableInFull(actionContext))
                            return false;
                    }

                    return true;
                }
            };
        };
    }
}