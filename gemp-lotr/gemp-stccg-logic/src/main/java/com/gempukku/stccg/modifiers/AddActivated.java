package com.gempukku.stccg.modifiers;

import com.gempukku.stccg.actions.sources.ActionSource;
import com.gempukku.stccg.actions.ActivateCardAction;
import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.sources.DefaultActionSource;
import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.effectappender.AbstractEffectAppender;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.turn.IncrementPhaseLimitEffect;
import com.gempukku.stccg.actions.turn.IncrementTurnLimitEffect;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.PlayConditions;
import org.json.simple.JSONObject;

import java.util.LinkedList;
import java.util.List;

public class AddActivated implements ModifierSourceProducer {
    @Override
    public ModifierSource getModifierSource(JSONObject object, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {
        environment.validateAllowedFields(object, "filter", "phase", "requires", "cost", "effect",
                "limitPerPhase", "limitPerTurn", "text");

        final FilterableSource filterableSource = environment.getFilterable(object);
        final String text = environment.getString(object.get("text"), "text");
        final String[] phaseArray = environment.getStringArray(object.get("phase"), "phase");
        final int limitPerPhase = environment.getInteger(object.get("limitPerPhase"), "limitPerPhase", 0);
        final int limitPerTurn = environment.getInteger(object.get("limitPerTurn"), "limitPerTurn", 0);

        if (phaseArray.length == 0)
            throw new InvalidCardDefinitionException("Unable to find phase for an activated effect");

        List<DefaultActionSource> actionSources = new LinkedList<>();

        for (String phaseString : phaseArray) {
            final Phase phase = Phase.valueOf(phaseString.toUpperCase());

            DefaultActionSource actionSource = new DefaultActionSource();
            actionSource.setText(text);
            if (limitPerPhase > 0) {
                actionSource.addRequirement(
                        actionContext -> PlayConditions.checkPhaseLimit(
                                actionContext.getGame(), actionContext.getSource(), phase, limitPerPhase
                        ));
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
                        (actionContext) -> PlayConditions.checkTurnLimit(actionContext.getGame(),
                                actionContext.getSource(), limitPerTurn));
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
            actionSource.processRequirementsCostsAndEffects(object, environment);

            actionSources.add(actionSource);
        }

        return actionContext -> new AddActionToCardModifier(actionContext.getSource(), null,
                filterableSource.getFilterable(actionContext)) {
            @Override
            public List<? extends ActivateCardAction> getExtraPhaseAction(DefaultGame game, PhysicalCard card) {
                LinkedList<ActivateCardAction> result = new LinkedList<>();
                for (ActionSource inPlayPhaseAction : actionSources) {
                    ActionContext actionContext = card.createActionContext();
                    if (inPlayPhaseAction.isValid(actionContext)) {
                        ActivateCardAction action = new ActivateCardAction(card);
                        inPlayPhaseAction.appendActionToContext(action, actionContext);
                        result.add(action);
                    }
                }

                return result;
            }

            @Override
            protected ActivateCardAction createExtraPhaseAction(DefaultGame game, PhysicalCard matchingCard) {
                return null;
            }
        };
    }
}
