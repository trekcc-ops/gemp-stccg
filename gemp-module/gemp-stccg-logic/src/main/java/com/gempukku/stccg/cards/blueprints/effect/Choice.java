package com.gempukku.stccg.cards.blueprints.effect;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.SubAction;
import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.blueprints.resolver.PlayerResolver;
import com.gempukku.stccg.decisions.MultipleChoiceAwaitingDecision;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.PlayOutDecisionEffect;
import com.gempukku.stccg.actions.StackActionEffect;
import com.gempukku.stccg.cards.blueprints.BlueprintUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Choice implements EffectAppenderProducer {
    @Override
    public EffectBlueprint createEffectAppender(JsonNode node, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {
        environment.validateAllowedFields(node, "player", "effects", "texts", "memorize");

        final String player = BlueprintUtils.getString(node, "player", "you");
        final JsonNode[] effectArray = environment.getNodeArray(node.get("effects"));
        final String[] textArray = environment.getStringArray(node.get("texts"));
        final String memorize = BlueprintUtils.getString(node, "memorize", "_temp");

        if (effectArray.length != textArray.length)
            throw new InvalidCardDefinitionException("Number of texts and effects does not match in choice effect");

        List<EffectBlueprint> possibleEffectBlueprints = new ArrayList<>();
        for (JsonNode effect : effectArray) {
            possibleEffectBlueprints.add(environment.getEffectAppenderFactory().getEffectAppender(effect));
        }

        final PlayerSource playerSource = PlayerResolver.resolvePlayer(player);

        return new DefaultDelayedAppender() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext context) {
                final String choosingPlayer = playerSource.getPlayerId(context);
                ActionContext delegateActionContext = context.createDelegateContext(choosingPlayer);

                int textIndex = 0;
                List<EffectBlueprint> playableEffectBlueprints = new LinkedList<>();
                List<String> effectTexts = new LinkedList<>();
                for (EffectBlueprint possibleEffectBlueprint : possibleEffectBlueprints) {
                    if (possibleEffectBlueprint.isPlayableInFull(delegateActionContext)) {
                        playableEffectBlueprints.add(possibleEffectBlueprint);
                        effectTexts.add(textArray[textIndex]);
                    }
                    textIndex++;
                }

                if (playableEffectBlueprints.isEmpty()) {
                    context.setValueToMemory(memorize, "");
                    return null;
                }

                if (playableEffectBlueprints.size() == 1) {
                    SubAction subAction = action.createSubAction();
                    playableEffectBlueprints.getFirst().addEffectToAction(cost, subAction, delegateActionContext);
                    context.setValueToMemory(memorize, textArray[0]);
                    return new StackActionEffect(context.getGame(), subAction);
                }

                SubAction subAction = action.createSubAction();
                subAction.appendCost(
                        new PlayOutDecisionEffect(context.getGame(), choosingPlayer,
                                new MultipleChoiceAwaitingDecision("Choose action to perform",
                                        effectTexts.toArray(new String[0])) {
                                    @Override
                                    protected void validDecisionMade(int index, String result) {
                                        playableEffectBlueprints.get(index)
                                                .addEffectToAction(cost, subAction, delegateActionContext);
                                        context.setValueToMemory(memorize, result);
                                    }
                                }));
                return new StackActionEffect(context.getGame(), subAction);
            }

            @Override
            public boolean isPlayableInFull(ActionContext actionContext) {
                final String choosingPlayer = playerSource.getPlayerId(actionContext);
                for (EffectBlueprint possibleEffectBlueprint : possibleEffectBlueprints) {
                    if (possibleEffectBlueprint.isPlayableInFull(actionContext.createDelegateContext(choosingPlayer)))
                        return true;
                }
                return false;
            }
        };
    }
}