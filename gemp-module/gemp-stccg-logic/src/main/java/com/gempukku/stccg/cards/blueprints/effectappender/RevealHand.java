package com.gempukku.stccg.cards.blueprints.effectappender;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.revealcards.RevealHandEffect;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.PlayerSource;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.blueprints.resolver.PlayerResolver;
import com.gempukku.stccg.game.DefaultGame;

import java.util.Collection;

public class RevealHand implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JsonNode effectObject, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        environment.validateAllowedFields(effectObject, "hand", "memorize");

        final String player = environment.getString(effectObject, "hand", "you");
        final PlayerSource playerSource = PlayerResolver.resolvePlayer(player);

        final String memorize = effectObject.get("memorize").textValue();

        return new DefaultDelayedAppender() {
            @Override
            public boolean isPlayableInFull(ActionContext actionContext) {
                final DefaultGame game = actionContext.getGame();
                final String revealingPlayer = playerSource.getPlayerId(actionContext);
                return game.getModifiersQuerying().canLookOrRevealCardsInHand(game, revealingPlayer, actionContext.getPerformingPlayerId());
            }

            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext context) {
                final String revealingPlayer = playerSource.getPlayerId(context);
                return new RevealHandEffect(context, revealingPlayer) {
                    @Override
                    protected void cardsRevealed(Collection<? extends PhysicalCard> cards) {
                        if (memorize != null)
                            context.setCardMemory(memorize, cards);
                    }
                };
            }
        };
    }

}
