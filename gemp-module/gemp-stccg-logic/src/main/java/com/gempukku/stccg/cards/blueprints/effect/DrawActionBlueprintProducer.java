package com.gempukku.stccg.cards.blueprints.effect;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.SubAction;
import com.gempukku.stccg.actions.draw.DrawCardAction;
import com.gempukku.stccg.actions.turn.StackActionEffect;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.PlayerSource;
import com.gempukku.stccg.cards.blueprints.BlueprintUtils;
import com.gempukku.stccg.cards.blueprints.ValueSource;
import com.gempukku.stccg.cards.blueprints.resolver.ValueResolver;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.Player;

import java.util.LinkedList;
import java.util.List;

public class DrawActionBlueprintProducer {

    public static EffectBlueprint createEffectBlueprint(JsonNode effectObject)
            throws InvalidCardDefinitionException {

        String[] allowedFields = new String[]{"count"};
        BlueprintUtils.validateAllowedFields(effectObject, allowedFields);

        final PlayerSource targetPlayerSource = BlueprintUtils.getTargetPlayerSource(effectObject);
        final ValueSource countSource =
                ValueResolver.resolveEvaluator(effectObject.get("count"), 1);

        return new DelayedEffectBlueprint() {
            @Override
            protected List<Action> createActions(Action action, ActionContext context) {
                final String targetPlayerId = targetPlayerSource.getPlayerId(context);
                DefaultGame cardGame = context.getGame();
                Player targetPlayer = cardGame.getPlayer(targetPlayerId);
                final int count = countSource.evaluateExpression(context, null);
                List<Action> result = new LinkedList<>();
                int numberOfEffects = 1;
                for (int i = 0; i < numberOfEffects; i++) {
                    Effect effect = new StackActionEffect(context.getGame(),
                                new DrawCardAction(context.getSource(), targetPlayer, count));
                    result.add(new SubAction(action, effect));
                }
                return result;
            }
            
            @Override
            public boolean isPlayableInFull(ActionContext context) {
                final int count = countSource.evaluateExpression(context, null);
                final String targetPlayerId = targetPlayerSource.getPlayerId(context);
                return context.getGameState().getDrawDeck(targetPlayerId).size() >= count;
            }
        };
    }
}