package com.gempukku.stccg.actions.movecard;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalNounCard1E;
import com.gempukku.stccg.cards.physicalcard.PhysicalShipCard;
import com.gempukku.stccg.actions.Effect;

import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.Player;

import java.util.*;

public class WalkCardsAction extends BeamOrWalkAction {

    public WalkCardsAction(Player player, PhysicalNounCard1E cardWalkingFrom) {
        super(player, cardWalkingFrom);
    }

    @Override
    protected Collection<PhysicalCard> getDestinationOptions() {
        Collection<PhysicalCard> result = new LinkedList<>();
        if (_cardSource instanceof PhysicalShipCard ship && ship.isDocked())
            result.add(ship.getDockedAtCard());
        else if (_cardSource instanceof FacilityCard)
            result.addAll(Filters.filter(
                    _cardSource.getAttachedCards(), _game, Filters.ship, Filters.your(_performingPlayer)));
        return result;
    }

    @Override
    protected List<PhysicalCard> getValidFromCards() {
        List<PhysicalCard> cards = new ArrayList<>();
        cards.add(_cardSource);
        return cards;
    }

    protected String getVerb() { return "walk"; }

    @Override
    protected Effect finalEffect() {
        return new BeamOrWalkCardsEffect(_cardsToMove, _fromCard, _toCard, _performingPlayerId, "Walk");
    }

    @Override
    public boolean canBeInitiated() {
            // TODO - No compatibility check
        return !_destinationOptions.isEmpty() &&
                !Filters.filter(_cardSource.getAttachedCards(), Filters.personnel).isEmpty();
    }

}