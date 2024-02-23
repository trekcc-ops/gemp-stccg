package com.gempukku.stccg.effectappender;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.SubAction;
import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
import com.gempukku.stccg.effectappender.resolver.PlayerResolver;
import com.gempukku.stccg.decisions.MultipleChoiceAwaitingDecision;
import com.gempukku.stccg.effects.Effect;
import com.gempukku.stccg.effects.PlayOutDecisionEffect;
import com.gempukku.stccg.effects.StackActionEffect;
import org.json.simple.JSONObject;

import java.util.LinkedList;
import java.util.List;

public class Choice implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "player", "effects", "texts", "memorize");

        final String player = FieldUtils.getString(effectObject.get("player"), "player", "you");
        final JSONObject[] effectArray = FieldUtils.getObjectArray(effectObject.get("effects"), "effects");
        final String[] textArray = FieldUtils.getStringArray(effectObject.get("texts"), "texts");
        final String memorize = FieldUtils.getString(effectObject.get("memorize"), "memorize", "_temp");

        if (effectArray.length != textArray.length)
            throw new InvalidCardDefinitionException("Number of texts and effects does not match in choice effect");

        EffectAppender[] possibleEffectAppenders = environment.getEffectAppenderFactory().getEffectAppenders(effectArray, environment);

        final PlayerSource playerSource = PlayerResolver.resolvePlayer(player);

        return new DefaultDelayedAppender() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                final String choosingPlayer = playerSource.getPlayerId(actionContext);
                ActionContext delegateActionContext = actionContext.createDelegateContext(choosingPlayer);

                int textIndex = 0;
                List<EffectAppender> playableEffectAppenders = new LinkedList<>();
                List<String> effectTexts = new LinkedList<>();
                for (EffectAppender possibleEffectAppender : possibleEffectAppenders) {
                    if (possibleEffectAppender.isPlayableInFull(delegateActionContext)) {
                        playableEffectAppenders.add(possibleEffectAppender);
                        effectTexts.add(textArray[textIndex]);
                    }
                    textIndex++;
                }

                if (playableEffectAppenders.isEmpty()) {
                    actionContext.setValueToMemory(memorize, "");
                    return null;
                }

                if (playableEffectAppenders.size() == 1) {
                    SubAction subAction = action.createSubAction();
                    playableEffectAppenders.get(0).appendEffect(cost, subAction, delegateActionContext);
                    actionContext.setValueToMemory(memorize, textArray[0]);
                    return new StackActionEffect(actionContext.getGame(), subAction);
                }

                SubAction subAction = action.createSubAction();
                subAction.appendCost(
                        new PlayOutDecisionEffect(actionContext.getGame(), choosingPlayer,
                                new MultipleChoiceAwaitingDecision(1, "Choose action to perform", effectTexts.toArray(new String[0])) {
                                    @Override
                                    protected void validDecisionMade(int index, String result) {
                                        playableEffectAppenders.get(index).appendEffect(cost, subAction, delegateActionContext);
                                        actionContext.setValueToMemory(memorize, result);
                                    }
                                }));
                return new StackActionEffect(actionContext.getGame(), subAction);
            }

            @Override
            public boolean isPlayableInFull(ActionContext actionContext) {
                final String choosingPlayer = playerSource.getPlayerId(actionContext);
                for (EffectAppender possibleEffectAppender : possibleEffectAppenders) {
                    if (possibleEffectAppender.isPlayableInFull(actionContext.createDelegateContext(choosingPlayer)))
                        return true;
                }
                return false;
            }
        };
    }
}
