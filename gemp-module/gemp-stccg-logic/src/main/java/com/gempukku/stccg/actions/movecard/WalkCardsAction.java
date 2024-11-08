package com.gempukku.stccg.actions.movecard;

import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalNounCard1E;
import com.gempukku.stccg.cards.physicalcard.PhysicalShipCard;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.ST1EGame;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class WalkCardsAction extends BeamOrWalkAction {

    public WalkCardsAction(Player player, PhysicalNounCard1E cardWalkingFrom) {
        super(player, cardWalkingFrom);
    }

    @Override
    protected Collection<PhysicalCard> getDestinationOptions(ST1EGame game) {
        Collection<PhysicalCard> result = new LinkedList<>();
        if (_cardSource instanceof PhysicalShipCard ship && ship.isDocked())
            result.add(ship.getDockedAtCard());
        else if (_cardSource instanceof FacilityCard)
            result.addAll(Filters.filter(
                    _cardSource.getAttachedCards(), game, Filters.ship, Filters.your(_performingPlayer)));
        return result;
    }

    @Override
    protected List<PhysicalCard> getValidFromCards() {
        List<PhysicalCard> cards = new ArrayList<>();
        cards.add(_cardSource);
        return cards;
    }

    protected String actionVerb() { return "walk"; }

    @Override
    public boolean canBeInitiated() {
            // TODO - No compatibility check
        return !_destinationOptions.isEmpty() &&
                !Filters.filter(_cardSource.getAttachedCards(), Filters.personnel).isEmpty();
    }

}