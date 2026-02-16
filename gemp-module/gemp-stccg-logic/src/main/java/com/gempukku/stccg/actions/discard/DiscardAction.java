package com.gempukku.stccg.actions.discard;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.ST1EPhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.DefaultGame;

import java.util.List;

public interface DiscardAction {

    @JsonProperty("destination")
    Zone getDestination();

    default void discardCard(PhysicalCard discardedCard, DefaultGame cardGame) {
        cardGame.removeCardsFromZone(List.of(discardedCard));
        if (discardedCard instanceof ST1EPhysicalCard stCard && stCard.isStopped()) {
            stCard.unstop();
        }
        cardGame.addCardToTopOfDiscardPile(discardedCard);
    }
}