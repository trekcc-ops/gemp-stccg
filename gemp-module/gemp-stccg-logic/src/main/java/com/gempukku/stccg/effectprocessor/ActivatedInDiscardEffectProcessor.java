package com.gempukku.stccg.effectprocessor;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.actions.sources.DefaultActionSource;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.common.filterable.Phase;

public class ActivatedInDiscardEffectProcessor implements EffectProcessor {
    @Override
    public void processEffect(JsonNode node, CardBlueprint blueprint, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {
        environment.validateAllowedFields(node,
                "phase", "requires", "cost", "effect", "limitPerPhase", "limitPerTurn", "text");

        final String text = node.get("text").textValue();
        final String[] phaseArray = environment.getStringArray(node.get("phase"));
        final int limitPerPhase = environment.getInteger(node, "limitPerPhase", 0);
        final int limitPerTurn = environment.getInteger(node, "limitPerTurn", 0);

        if (phaseArray.length == 0)
            throw new InvalidCardDefinitionException("Unable to find phase for an activated effect");

        for (String phaseString : phaseArray) {
            final Phase phase = Phase.valueOf(phaseString.toUpperCase());

            DefaultActionSource actionSource = new DefaultActionSource();
            actionSource.setText(text);
            if (limitPerPhase > 0)
                actionSource.setPhaseLimit(phase, limitPerPhase);
            if (limitPerTurn > 0)
                actionSource.setTurnLimit(limitPerTurn);
            actionSource.addRequirement(
                    (actionContext) -> actionContext.getGameState().getCurrentPhase() == phase);
            actionSource.processRequirementsCostsAndEffects(node, environment);

            blueprint.appendInDiscardPhaseAction(actionSource);
        }
    }
}