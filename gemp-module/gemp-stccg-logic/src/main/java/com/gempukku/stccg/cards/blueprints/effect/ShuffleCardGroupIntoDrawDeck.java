package com.gempukku.stccg.cards.blueprints.effect;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.UnrespondableEffect;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.PlayerSource;
import com.gempukku.stccg.cards.blueprints.BlueprintUtils;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;

import java.util.LinkedList;
import java.util.List;

public class ShuffleCardGroupIntoDrawDeck implements EffectAppenderProducer {
    private final Zone _cardGroup;
    public ShuffleCardGroupIntoDrawDeck(Zone cardGroup) {
        _cardGroup = cardGroup;
    }
    @Override
    public EffectBlueprint createEffectAppender(JsonNode effectObject, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {

        BlueprintUtils.validateAllowedFields(effectObject, "player");
        final PlayerSource playerSource = BlueprintUtils.getPlayerSource(effectObject, "player", true);

        return new DefaultDelayedAppender() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action,
                                          ActionContext context) {
                final String cardGroupOwner = playerSource.getPlayerId(context);
                return new UnrespondableEffect(context) {
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