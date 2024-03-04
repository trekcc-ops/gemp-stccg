package com.gempukku.stccg.effectappender;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.UnrespondableEffect;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.PlayerSource;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.effectappender.resolver.PlayerResolver;
import org.json.simple.JSONObject;

import java.util.LinkedList;
import java.util.List;

public class ShuffleCardGroupIntoDrawDeck implements EffectAppenderProducer {
    private final Zone _cardGroup;
    public ShuffleCardGroupIntoDrawDeck(Zone cardGroup) {
        _cardGroup = cardGroup;
    }
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {
        environment.validateAllowedFields(effectObject, "player");

        String player = environment.getString(effectObject.get("player"), "player", "you");
        final PlayerSource playerSource = PlayerResolver.resolvePlayer(player);

        return new DefaultDelayedAppender() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action,
                                          ActionContext context) {
                final String cardGroupOwner = playerSource.getPlayerId(context);
                return new UnrespondableEffect() {
                    @Override
                    protected void doPlayEffect() {
                        List<PhysicalCard> cardGroup =
                                new LinkedList<>(context.getGameState().getZoneCards(
                                        cardGroupOwner, _cardGroup));
                        context.getGameState().removeCardsFromZone(
                                context.getPerformingPlayerId(), cardGroup);
                        cardGroup.forEach(context.getGameState()::putCardOnBottomOfDeck);
                        context.getGameState().shuffleDeck(cardGroupOwner);
                    }
                };
            }
        };
    }
}