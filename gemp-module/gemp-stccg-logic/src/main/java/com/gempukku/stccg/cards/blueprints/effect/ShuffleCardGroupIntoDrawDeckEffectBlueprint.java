package com.gempukku.stccg.cards.blueprints.effect;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.UnrespondableEffect;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.PlayerSource;
import com.gempukku.stccg.cards.blueprints.BlueprintUtils;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;

import java.util.LinkedList;
import java.util.List;

public class ShuffleCardGroupIntoDrawDeckEffectBlueprint extends DelayedEffectBlueprint {
    private final Zone _cardGroup;
    private final PlayerSource _player;
    public ShuffleCardGroupIntoDrawDeckEffectBlueprint(JsonNode effectObject, Zone cardGroup)
            throws InvalidCardDefinitionException {
        _cardGroup = cardGroup;
        BlueprintUtils.validateAllowedFields(effectObject, "player");
        _player = BlueprintUtils.getPlayerSource(effectObject, "player", true);
    }

    @Override
    protected Effect createEffect(boolean cost, CostToEffectAction action,
                                  ActionContext context) {
        final String cardGroupOwner = _player.getPlayerId(context);
        return new UnrespondableEffect(context) {
            @Override
            protected void doPlayEffect() {
                List<PhysicalCard> cardGroup =
                        new LinkedList<>(context.getGameState().getZoneCards(cardGroupOwner, _cardGroup));
                context.getGameState().removeCardsFromZone(context.getPerformingPlayerId(), cardGroup);
                cardGroup.forEach(context.getGameState()::putCardOnBottomOfDeck);
                context.getGameState().shuffleDeck(cardGroupOwner);
            }
        };
    }
}