package com.gempukku.stccg.effectprocessor;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.actions.sources.DefaultActionSource;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
import com.gempukku.stccg.effectappender.AbstractEffectAppender;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.effects.Effect;
import com.gempukku.stccg.effects.defaulteffect.unrespondable.IncrementPhaseLimitEffect;
import com.gempukku.stccg.effects.defaulteffect.unrespondable.IncrementTurnLimitEffect;
import com.gempukku.stccg.game.PlayConditions;
import org.json.simple.JSONObject;

public class ActivatedInDiscardEffectProcessor implements EffectProcessor {
    @Override
    public void processEffect(JSONObject value, BuiltCardBlueprint blueprint, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(value, "phase", "requires", "cost", "effect", "limitPerPhase", "limitPerTurn", "text");

        final String text = FieldUtils.getString(value.get("text"), "text");
        final String[] phaseArray = FieldUtils.getStringArray(value.get("phase"), "phase");
        final int limitPerPhase = FieldUtils.getInteger(value.get("limitPerPhase"), "limitPerPhase", 0);
        final int limitPerTurn = FieldUtils.getInteger(value.get("limitPerTurn"), "limitPerTurn", 0);

        if (phaseArray.length == 0)
            throw new InvalidCardDefinitionException("Unable to find phase for an activated effect");

        for (String phaseString : phaseArray) {
            final Phase phase = Phase.valueOf(phaseString.toUpperCase());

            DefaultActionSource actionSource = new DefaultActionSource();
            actionSource.setText(text);
            if (limitPerPhase > 0) {
                actionSource.addPlayRequirement(
                        (actionContext) -> PlayConditions.checkPhaseLimit(actionContext.getGame(), actionContext.getSource(), phase, limitPerPhase));
                actionSource.addCost(
                        new AbstractEffectAppender() {
                            @Override
                            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                                return new IncrementPhaseLimitEffect(actionContext, phase, limitPerPhase);
                            }
                        });
            }
            if (limitPerTurn > 0) {
                actionSource.addPlayRequirement(
                        (actionContext) -> PlayConditions.checkTurnLimit(actionContext.getGame(), actionContext.getSource(), limitPerTurn));
                actionSource.addCost(
                        new AbstractEffectAppender() {
                            @Override
                            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                                return new IncrementTurnLimitEffect(actionContext, limitPerTurn);
                            }
                        });
            }
            actionSource.addPlayRequirement(
                    (actionContext) -> actionContext.getGameState().getCurrentPhase() == phase);
            EffectUtils.processRequirementsCostsAndEffects(value, environment, actionSource);

            blueprint.appendInDiscardPhaseAction(actionSource);
        }
    }
}