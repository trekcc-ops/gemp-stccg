package com.gempukku.stccg.cards.blueprints.effect;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.CardPerformedAction;
import com.gempukku.stccg.actions.draw.DrawCardsAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.PlayerSource;
import com.gempukku.stccg.cards.blueprints.BlueprintUtils;
import com.gempukku.stccg.cards.blueprints.ValueSource;
import com.gempukku.stccg.cards.blueprints.resolver.ValueResolver;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.PlayerNotFoundException;

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
            protected List<Action> createActions(CardPerformedAction action, ActionContext context) throws PlayerNotFoundException {
                final String targetPlayerId = targetPlayerSource.getPlayerId(context);
                DefaultGame cardGame = context.getGame();
                Player targetPlayer = cardGame.getPlayer(targetPlayerId);
                final int count = countSource.evaluateExpression(context, null);
                List<Action> result = new LinkedList<>();
                int numberOfEffects = 1;
                for (int i = 0; i < numberOfEffects; i++) {
                    result.add(new DrawCardsAction(context.getSource(), targetPlayer, count));
                }
                return result;
            }
            
            @Override
            public boolean isPlayableInFull(ActionContext context) {
                try {
                    final int count = countSource.evaluateExpression(context, null);
                    final String targetPlayerId = targetPlayerSource.getPlayerId(context);
                    Player targetPlayer = context.getGame().getPlayer(targetPlayerId);
                    return targetPlayer.getCardsInDrawDeck().size() >= count;
                } catch(PlayerNotFoundException exp) {
                    context.getGame().sendErrorMessage(exp);
                    return false;
                }
            }
        };
    }
}