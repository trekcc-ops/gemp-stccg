package com.gempukku.stccg.cards.blueprints.effect;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.revealcards.LookAtOpponentsHandEffect;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.PlayerSource;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;

public class LookAtHand implements EffectAppenderProducer {
    @Override
    public EffectBlueprint createEffectAppender(JsonNode effectObject, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {

        environment.validateAllowedFields(effectObject, "targetPlayer");
        final PlayerSource targetPlayer =
                environment.getPlayerSource(effectObject, "targetPlayer", true);

        return new DefaultDelayedAppender() {
            @Override
            public boolean isPlayableInFull(ActionContext actionContext) {
                return actionContext.getPerformingPlayer().canLookOrRevealCardsInHandOfPlayer(
                        targetPlayer.getPlayerId(actionContext));
            }

            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext context) {
                return new LookAtOpponentsHandEffect(context, targetPlayer.getPlayerId(context));
            }
        };
    }

}