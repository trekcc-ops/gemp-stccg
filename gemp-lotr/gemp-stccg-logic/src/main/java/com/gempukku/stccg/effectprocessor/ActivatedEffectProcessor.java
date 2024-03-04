package com.gempukku.stccg.effectprocessor;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.sources.ActivateCardActionSource;
import com.gempukku.stccg.actions.sources.DefaultActionSource;
import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.effectappender.AbstractEffectAppender;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.turn.IncrementPhaseLimitEffect;
import com.gempukku.stccg.actions.turn.IncrementTurnLimitEffect;
import com.gempukku.stccg.game.PlayConditions;
import org.json.simple.JSONObject;

public class ActivatedEffectProcessor implements EffectProcessor {
    @Override
    public void processEffect(JSONObject value, CardBlueprint blueprint, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        environment.validateAllowedFields(value, "phase", "requires", "cost", "effect", "limitPerPhase", "limitPerTurn", "text");

        final String text = environment.getString(value.get("text"), "text");
        final String[] phaseArray = environment.getStringArray(value.get("phase"), "phase");
        final int limitPerPhase = environment.getInteger(value.get("limitPerPhase"), "limitPerPhase", 0);
        final int limitPerTurn = environment.getInteger(value.get("limitPerTurn"), "limitPerTurn", 0);

        if (phaseArray.length == 0)
            throw new InvalidCardDefinitionException("Unable to find phase for an activated effect");

        for (String phaseString : phaseArray) {
            final Phase phase = environment.getEnum(Phase.class, phaseString);

            DefaultActionSource actionSource = new ActivateCardActionSource();
            actionSource.setText(text);
            if (limitPerPhase > 0) {
                actionSource.addRequirement(
                        (actionContext) -> PlayConditions.checkPhaseLimit(actionContext.getGame(),
                                actionContext.getSource(), phase, limitPerPhase));
                actionSource.addCost(
                        new AbstractEffectAppender() {
                            @Override
                            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                                return new IncrementPhaseLimitEffect(actionContext, phase, limitPerPhase);
                            }
                        });
            }
            if (limitPerTurn > 0) {
                actionSource.addRequirement(
                        (actionContext) -> PlayConditions.checkTurnLimit(actionContext.getSource(), limitPerTurn));
                actionSource.addCost(
                        new AbstractEffectAppender() {
                            @Override
                            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                                return new IncrementTurnLimitEffect(actionContext, limitPerTurn);
                            }
                        });
            }
            actionSource.addRequirement(
                    (actionContext) -> actionContext.getGameState().getCurrentPhase() == phase);
            actionSource.processRequirementsCostsAndEffects(value, environment);
            blueprint.appendInPlayPhaseAction(actionSource);
        }
    }
}