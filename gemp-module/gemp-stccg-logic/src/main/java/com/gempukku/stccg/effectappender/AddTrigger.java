package com.gempukku.stccg.effectappender;

import com.gempukku.stccg.actions.*;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.effectappender.resolver.TimeResolver;
import com.gempukku.stccg.actions.turn.AddUntilEndOfPhaseActionProxyEffect;
import com.gempukku.stccg.actions.turn.AddUntilEndOfTurnActionProxyEffect;
import com.gempukku.stccg.actions.turn.AddUntilStartOfPhaseActionProxyEffect;
import com.gempukku.stccg.requirement.Requirement;
import com.gempukku.stccg.requirement.trigger.TriggerChecker;
import org.json.simple.JSONObject;

import java.util.Collections;
import java.util.List;

public class AddTrigger implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {
        environment.validateAllowedFields(effectObject,
                "trigger", "until", "optional", "requires", "cost", "effect");

        final TimeResolver.Time until = TimeResolver.resolveTime(effectObject.get("until"), "end(current)");
        final TriggerChecker trigger = environment.getTriggerCheckerFactory().getTriggerChecker(
                (JSONObject) effectObject.get("trigger"), environment);
        final boolean optional = environment.getBoolean(effectObject.get("optional"), "optional", false);

        final Requirement[] requirements = environment.getRequirementsFromJSON(effectObject);
        final EffectAppender[] costs = environment.getEffectAppendersFromJSON(effectObject,"cost");
        final EffectAppender[] effects = environment.getEffectAppendersFromJSON(effectObject,"effect");

        return new DefaultDelayedAppender() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext context) {
                ActionProxy actionProxy =
                        createActionProxy(context, optional, trigger, requirements, costs, effects);

                if (until.isEndOfTurn()) {
                    return new AddUntilEndOfTurnActionProxyEffect(context.getGame(), actionProxy);
                } else if (until.isStart()) {
                    return new AddUntilStartOfPhaseActionProxyEffect(
                            context.getGame(), actionProxy, until.getPhase());
                } else {
                    return new AddUntilEndOfPhaseActionProxyEffect(
                            context.getGame(), actionProxy, until.getPhase());
                }
            }
        };
    }

    private ActionProxy createActionProxy(ActionContext actionContext, boolean optional, TriggerChecker trigger,
                                          Requirement[] requirements, EffectAppender[] costs,
                                          EffectAppender[] effects) {
        return new AbstractActionProxy() {
            private boolean checkRequirements(ActionContext actionContext) {
                if (actionContext.acceptsAllRequirements(requirements))
                    return true;

                for (EffectAppender cost : costs) {
                    if (!cost.isPlayableInFull(actionContext))
                        return true;
                }
                return false;
            }

            private void customizeTriggerAction(AbstractCostToEffectAction action, ActionContext actionContext) {
                action.setVirtualCardAction(true);
                for (EffectAppender cost : costs)
                    cost.appendEffect(true, action, actionContext);
                for (EffectAppender effectAppender : effects)
                    effectAppender.appendEffect(false, action, actionContext);
            }

            @Override
            public List<? extends Action> getRequiredBeforeTriggers(Effect effect) {
                ActionContext delegateContext = actionContext.createDelegateContext(effect);
                if (trigger.isBefore() && !optional && trigger.accepts(delegateContext)) {
                    if (checkRequirements(delegateContext))
                        return null;

                    RequiredTriggerAction result = new RequiredTriggerAction(delegateContext.getSource());
                    customizeTriggerAction(result, delegateContext);

                    return Collections.singletonList(result);
                }
                return null;
            }

            @Override
            public List<? extends Action> getOptionalBeforeTriggerActions(String playerId, Effect effect) {
                ActionContext delegateContext = actionContext.createDelegateContext(effect);
                if (trigger.isBefore() && optional && trigger.accepts(delegateContext)) {
                    if (checkRequirements(delegateContext))
                        return null;

                    OptionalTriggerAction result = new OptionalTriggerAction(delegateContext.getSource());
                    customizeTriggerAction(result, delegateContext);

                    return Collections.singletonList(result);
                }
                return null;
            }

            @Override
            public List<? extends RequiredTriggerAction> getRequiredAfterTriggers(EffectResult effectResult) {
                ActionContext delegate = actionContext.createDelegateContext(effectResult);
                if (!trigger.isBefore() && !optional && trigger.accepts(delegate)) {
                    if (checkRequirements(delegate))
                        return null;

                    RequiredTriggerAction result = new RequiredTriggerAction(delegate.getSource());
                    customizeTriggerAction(result, delegate);

                    return Collections.singletonList(result);
                }
                return null;
            }

        };
    }
}
