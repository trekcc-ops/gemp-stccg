package com.gempukku.stccg.effectappender;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.cards.CardGenerationEnvironment;
import com.gempukku.stccg.cards.DefaultActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.PlayerSource;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
import com.gempukku.stccg.effectappender.resolver.PlayerResolver;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.effects.Effect;
import com.gempukku.stccg.effects.RevealHandEffect;
import com.gempukku.stccg.game.DefaultGame;
import org.json.simple.JSONObject;

import java.util.Collection;

public class RevealHand implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "hand", "memorize");

        final String player = FieldUtils.getString(effectObject.get("hand"), "hand", "you");
        final PlayerSource playerSource = PlayerResolver.resolvePlayer(player);

        final String memorize = FieldUtils.getString(effectObject.get("memorize"), "memorize");

        return new DelayedAppender<>() {
            @Override
            public boolean isPlayableInFull(DefaultActionContext<DefaultGame> actionContext) {
                final DefaultGame game = actionContext.getGame();
                final String revealingPlayer = playerSource.getPlayer(actionContext);
                return game.getModifiersQuerying().canLookOrRevealCardsInHand(game, revealingPlayer, actionContext.getPerformingPlayer());
            }

            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, DefaultActionContext actionContext) {
                final String revealingPlayer = playerSource.getPlayer(actionContext);
                return new RevealHandEffect(actionContext.getSource(), actionContext.getPerformingPlayer(), revealingPlayer) {
                    @Override
                    protected void cardsRevealed(Collection<? extends PhysicalCard> cards) {
                        if (memorize != null)
                            actionContext.setCardMemory(memorize, cards);
                    }
                };
            }
        };
    }

}
