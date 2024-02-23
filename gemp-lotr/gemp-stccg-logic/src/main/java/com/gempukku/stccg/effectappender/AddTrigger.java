package com.gempukku.stccg.effectappender;

import com.gempukku.stccg.actions.*;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.CardGenerationEnvironment;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.effectappender.resolver.TimeResolver;
import com.gempukku.stccg.effects.*;
import com.gempukku.stccg.effects.defaulteffect.unrespondable.AddUntilEndOfPhaseActionProxyEffect;
import com.gempukku.stccg.effects.defaulteffect.unrespondable.AddUntilEndOfTurnActionProxyEffect;
import com.gempukku.stccg.effects.defaulteffect.unrespondable.AddUntilStartOfPhaseActionProxyEffect;
import com.gempukku.stccg.results.EffectResult;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
import com.gempukku.stccg.requirement.Requirement;
import com.gempukku.stccg.requirement.RequirementUtils;
import com.gempukku.stccg.requirement.trigger.TriggerChecker;
import org.json.simple.JSONObject;

import java.util.Collections;
import java.util.List;

public class AddTrigger implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "trigger", "until", "optional", "requires", "cost", "effect");

        final TimeResolver.Time until = TimeResolver.resolveTime(effectObject.get("until"), "end(current)");
        final TriggerChecker trigger = environment.getTriggerCheckerFactory().getTriggerChecker((JSONObject) effectObject.get("trigger"), environment);
        final boolean optional = FieldUtils.getBoolean(effectObject.get("optional"), "optional", false);

        final JSONObject[] requirementArray = FieldUtils.getObjectArray(effectObject.get("requires"), "requires");
        final JSONObject[] costArray = FieldUtils.getObjectArray(effectObject.get("cost"), "cost");
        final JSONObject[] effectArray = FieldUtils.getObjectArray(effectObject.get("effect"), "effect");

        final Requirement[] requirements = environment.getRequirementFactory().getRequirements(requirementArray, environment);
        final EffectAppender[] costs = environment.getEffectAppenderFactory().getEffectAppenders(costArray, environment);
        final EffectAppender[] effects = environment.getEffectAppenderFactory().getEffectAppenders(effectArray, environment);

        return new DefaultDelayedAppender() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                ActionProxy actionProxy = createActionProxy(actionContext, optional, trigger, requirements, costs, effects);

                if (until.isEndOfTurn()) {
                    return new AddUntilEndOfTurnActionProxyEffect(actionContext.getGame(), actionProxy);
                } else if (until.isStart()) {
                    return new AddUntilStartOfPhaseActionProxyEffect(actionContext.getGame(), actionProxy, until.getPhase());
                } else {
                    return new AddUntilEndOfPhaseActionProxyEffect(actionContext.getGame(), actionProxy, until.getPhase());
                }
            }
        };
    }

    private ActionProxy createActionProxy(ActionContext actionContext, boolean optional, TriggerChecker trigger,
                                          Requirement[] requirements, EffectAppender[] costs, EffectAppender[] effects) {
        return new AbstractActionProxy() {
            private boolean checkRequirements(ActionContext actionContext) {
                if (!RequirementUtils.acceptsAllRequirements(requirements, actionContext))
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
            public List<? extends Action> getOptionalBeforeTriggers(String playerId, Effect effect) {
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

            @Override
            public List<? extends OptionalTriggerAction> getOptionalAfterTriggerActions(String playerId, EffectResult effectResult) {
                ActionContext delegate = actionContext.createDelegateContext(effectResult);
                if (!trigger.isBefore() && optional && trigger.accepts(delegate)) {
                    if (checkRequirements(delegate))
                        return null;

                    OptionalTriggerAction result = new OptionalTriggerAction(delegate.getSource());
                    customizeTriggerAction(result, delegate);

                    return Collections.singletonList(result);
                }
                return null;
            }
        };
    }
}
