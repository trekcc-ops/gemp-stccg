package com.gempukku.stccg.effectappender;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.effectappender.resolver.PlayerResolver;
import com.gempukku.stccg.effects.Effect;
import com.gempukku.stccg.effects.defaulteffect.UnrespondableEffect;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
import com.gempukku.stccg.gamestate.TribblesGameState;
import org.json.simple.JSONObject;

import java.util.LinkedList;
import java.util.List;

public class ShuffleCardGroupIntoDrawDeck implements EffectAppenderProducer {
    private final Zone _cardGroup;
    public ShuffleCardGroupIntoDrawDeck(Zone cardGroup) {
        _cardGroup = cardGroup;
    }
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment)
            throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "player");

        String player = FieldUtils.getString(effectObject.get("player"), "player", "you");
        final PlayerSource playerSource = PlayerResolver.resolvePlayer(player);

        return new DefaultDelayedAppender() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action,
                                          ActionContext actionContext) {
                final String cardGroupOwner = playerSource.getPlayerId(actionContext);
                return new UnrespondableEffect() {
                    @Override
                    protected void doPlayEffect() {
                        List<PhysicalCard> cardGroup =
                                new LinkedList<>(actionContext.getGame().getGameState().getZoneCards(
                                        cardGroupOwner, _cardGroup));
                        actionContext.getGame().getGameState().removeCardsFromZone(
                                actionContext.getPerformingPlayer(), cardGroup);
                        cardGroup.forEach(actionContext.getGame().getGameState()::putCardOnBottomOfDeck);
                        actionContext.getGame().getGameState().shuffleDeck(cardGroupOwner);
                    }
                };
            }
        };
    }
}