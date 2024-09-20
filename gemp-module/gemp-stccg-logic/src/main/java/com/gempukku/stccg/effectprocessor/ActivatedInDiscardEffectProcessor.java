package com.gempukku.stccg.effectprocessor;

import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.actions.sources.DefaultActionSource;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.common.filterable.Phase;
import org.json.simple.JSONObject;

public class ActivatedInDiscardEffectProcessor implements EffectProcessor {
    @Override
    public void processEffect(JSONObject value, CardBlueprint blueprint, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {
        environment.validateAllowedFields(value,
                "phase", "requires", "cost", "effect", "limitPerPhase", "limitPerTurn", "text");

        final String text = environment.getString(value.get("text"), "text");
        final String[] phaseArray = environment.getStringArray(value.get("phase"), "phase");
        final int limitPerPhase = environment.getInteger(value.get("limitPerPhase"), "limitPerPhase", 0);
        final int limitPerTurn = environment.getInteger(value.get("limitPerTurn"), "limitPerTurn", 0);

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
            actionSource.processRequirementsCostsAndEffects(value, environment);

            blueprint.appendInDiscardPhaseAction(actionSource);
        }
    }
}