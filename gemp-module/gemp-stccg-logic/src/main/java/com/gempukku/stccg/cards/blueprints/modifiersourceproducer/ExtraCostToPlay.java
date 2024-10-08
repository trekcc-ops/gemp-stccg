package com.gempukku.stccg.cards.blueprints.modifiersourceproducer;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.blueprints.BlueprintUtils;
import com.gempukku.stccg.cards.blueprints.FilterableSource;
import com.gempukku.stccg.cards.blueprints.effect.EffectBlueprint;
import com.gempukku.stccg.cards.blueprints.effect.EffectBlueprintDeserializer;
import com.gempukku.stccg.cards.blueprints.requirement.Requirement;
import com.gempukku.stccg.cards.blueprints.requirement.RequirementFactory;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.modifiers.AbstractExtraPlayCostModifier;
import com.gempukku.stccg.modifiers.RequirementCondition;

import java.util.List;

public class ExtraCostToPlay implements ModifierSourceProducer {
    @Override
    public ModifierSource getModifierSource(JsonNode node)
            throws InvalidCardDefinitionException {
        BlueprintUtils.validateAllowedFields(node, "requires", "cost", "filter");

        final FilterableSource filterableSource = BlueprintUtils.getFilterable(node);
        final Requirement[] requirements = new RequirementFactory().getRequirements(node);
        final List<EffectBlueprint> effectBlueprints = 
                EffectBlueprintDeserializer.getEffectBlueprints(node.get("cost"));

        return (actionContext) -> {
            final Filterable filterable = filterableSource.getFilterable(actionContext);
            final RequirementCondition condition = new RequirementCondition(requirements, actionContext);

            return new AbstractExtraPlayCostModifier(actionContext.getSource(),
                    "Cost to play is modified", filterable, condition) {
                @Override
                public void appendExtraCosts(DefaultGame game, CostToEffectAction action, PhysicalCard card) {
                    for (EffectBlueprint effectBlueprint : effectBlueprints)
                        effectBlueprint.addEffectToAction(true, action, actionContext);
                }

                @Override
                public boolean canPayExtraCostsToPlay(DefaultGame game, PhysicalCard card) {
                    for (EffectBlueprint effectBlueprint : effectBlueprints) {
                        if (!effectBlueprint.isPlayableInFull(actionContext))
                            return false;
                    }

                    return true;
                }
            };
        };
    }
}