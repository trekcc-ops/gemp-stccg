package com.gempukku.stccg.effectappender;

import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
import com.gempukku.stccg.effectappender.resolver.PlayerResolver;
import com.gempukku.stccg.effectappender.resolver.ValueResolver;
import com.gempukku.stccg.rules.GameUtils;
import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.decisions.DecisionResultInvalidException;
import com.gempukku.stccg.decisions.IntegerAwaitingDecision;
import com.gempukku.stccg.effects.PlayoutDecisionEffect;
import com.gempukku.stccg.effects.Effect;
import org.json.simple.JSONObject;

public class ChooseANumber implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "player", "text", "from", "to", "memorize");

        final String player = FieldUtils.getString(effectObject.get("player"), "player", "you");
        final String displayText = FieldUtils.getString(effectObject.get("text"), "text", "Choose a number");
        final ValueSource fromSource = ValueResolver.resolveEvaluator(effectObject.get("from"), 0, environment);
        final ValueSource toSource = ValueResolver.resolveEvaluator(effectObject.get("to"), 1, environment);

        final String memorize = FieldUtils.getString(effectObject.get("memorize"), "memorize");

        final PlayerSource playerSource = PlayerResolver.resolvePlayer(player);

        if (memorize == null)
            throw new InvalidCardDefinitionException("ChooseANumber requires a field to memorize the value");

        return new DelayedAppender<>() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, DefaultActionContext actionContext) {
                return new PlayoutDecisionEffect(actionContext.getPerformingPlayer(),
                    new IntegerAwaitingDecision(1, GameUtils.SubstituteText(displayText, actionContext),
                        fromSource.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), null),
                        toSource.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), null))
                {
                    @Override
                    public void decisionMade(String result) throws DecisionResultInvalidException {
                        actionContext.setValueToMemory(memorize, String.valueOf(getValidatedResult(result)));
                    }
                });
            }
        };
    }
}