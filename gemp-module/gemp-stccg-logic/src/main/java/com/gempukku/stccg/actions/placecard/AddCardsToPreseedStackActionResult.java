package com.gempukku.stccg.actions.placecard;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionResultType;
import com.gempukku.stccg.actions.NoResponseActionResult;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

import java.util.Collection;
import java.util.Objects;

public class AddCardsToPreseedStackActionResult extends NoResponseActionResult {

    private final Collection<PhysicalCard> _cardsAdded;
    public AddCardsToPreseedStackActionResult(DefaultGame cardGame, String performingPlayerId, Action action,
                                              Collection<PhysicalCard> cardsBeingSeeded) {
        super(cardGame, ActionResultType.ADD_CARDS_TO_PRESEED_STACK, performingPlayerId, action);
        _cardsAdded = cardsBeingSeeded;
    }

    @JsonProperty("targetCardIds")
    @JsonIdentityReference(alwaysAsId=true)
    private Collection<PhysicalCard> getCardsAdded() {
        return _cardsAdded;
    }

    @Override
    public boolean isKnownToPlayer(String playerName) {
        return Objects.equals(playerName, _performingPlayerId);
    }
}