package com.gempukku.stccg.actions.placecard;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionResultType;
import com.gempukku.stccg.actions.NoResponseActionResult;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

import java.util.ArrayList;
import java.util.Collection;

public class PlaceCardInDrawDeckResult extends NoResponseActionResult {

    public enum Placement { BOTTOM, TOP, SHUFFLE }

    @JsonProperty("placement")
    private final Placement _placement;

    private final boolean _showOpponent;

    @JsonProperty("targetCardIds")
    @JsonIdentityReference(alwaysAsId=true)
    private final Collection<PhysicalCard> _cardsPlaced = new ArrayList<>();

    public PlaceCardInDrawDeckResult(DefaultGame cardGame, Action action, Placement placement,
                                     Collection<PhysicalCard> cardsPlaced, boolean showOpponent) {
        super(cardGame, ActionResultType.PLACED_CARDS_IN_DRAW_DECK, action.getPerformingPlayerId(), action);
        _placement = placement;
        _cardsPlaced.addAll(cardsPlaced);
        _showOpponent = showOpponent;
    }
}