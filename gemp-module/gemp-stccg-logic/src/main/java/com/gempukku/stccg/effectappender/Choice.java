package com.gempukku.stccg.effectappender;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.SubAction;
import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.effectappender.resolver.PlayerResolver;
import com.gempukku.stccg.decisions.MultipleChoiceAwaitingDecision;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.PlayOutDecisionEffect;
import com.gempukku.stccg.actions.StackActionEffect;
import org.json.simple.JSONObject;

import java.util.LinkedList;
import java.util.List;

public class Choice implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        environment.validateAllowedFields(effectObject, "player", "effects", "texts", "memorize");

        final String player = environment.getString(effectObject.get("player"), "player", "you");
        final JSONObject[] effectArray = environment.getObjectArray(effectObject.get("effects"), "effects");
        final String[] textArray = environment.getStringArray(effectObject.get("texts"), "texts");
        final String memorize = environment.getString(effectObject.get("memorize"), "memorize", "_temp");

        if (effectArray.length != textArray.length)
            throw new InvalidCardDefinitionException("Number of texts and effects does not match in choice effect");

        EffectAppender[] possibleEffectAppenders = environment.getEffectAppenderFactory().getEffectAppenders(effectArray);

        final PlayerSource playerSource = PlayerResolver.resolvePlayer(player);

        return new DefaultDelayedAppender() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext context) {
                final String choosingPlayer = playerSource.getPlayerId(context);
                ActionContext delegateActionContext = context.createDelegateContext(choosingPlayer);

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
                    context.setValueToMemory(memorize, "");
                    return null;
                }

                if (playableEffectAppenders.size() == 1) {
                    SubAction subAction = action.createSubAction();
                    playableEffectAppenders.get(0).appendEffect(cost, subAction, delegateActionContext);
                    context.setValueToMemory(memorize, textArray[0]);
                    return new StackActionEffect(context.getGame(), subAction);
                }

                SubAction subAction = action.createSubAction();
                subAction.appendCost(
                        new PlayOutDecisionEffect(context.getGame(), choosingPlayer,
                                new MultipleChoiceAwaitingDecision("Choose action to perform", effectTexts.toArray(new String[0])) {
                                    @Override
                                    protected void validDecisionMade(int index, String result) {
                                        playableEffectAppenders.get(index).appendEffect(cost, subAction, delegateActionContext);
                                        context.setValueToMemory(memorize, result);
                                    }
                                }));
                return new StackActionEffect(context.getGame(), subAction);
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
