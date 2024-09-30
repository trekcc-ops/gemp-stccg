package com.gempukku.stccg.cards.blueprints.effectappender;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.revealcards.LookAtOpponentsHandEffect;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.PlayerSource;
import com.gempukku.stccg.game.DefaultGame;

public class LookAtHand implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JsonNode effectObject, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {

        environment.validateAllowedFields(effectObject, "hand");
        final PlayerSource playerSource = environment.getPlayerSource(effectObject, "hand", true);

        return new DefaultDelayedAppender() {
            @Override
            public boolean isPlayableInFull(ActionContext actionContext) {
                final DefaultGame game = actionContext.getGame();
                final String revealingPlayer = playerSource.getPlayerId(actionContext);
                return game.getModifiersQuerying().canLookOrRevealCardsInHand(
                        game, revealingPlayer, actionContext.getPerformingPlayerId());
            }

            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext context) {
                final String revealingPlayer = playerSource.getPlayerId(context);
                return new LookAtOpponentsHandEffect(context, revealingPlayer);
            }
        };
    }

}
