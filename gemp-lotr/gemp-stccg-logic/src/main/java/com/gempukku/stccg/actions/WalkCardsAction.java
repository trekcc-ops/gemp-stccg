package com.gempukku.stccg.actions;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.cards.PhysicalFacilityCard;
import com.gempukku.stccg.cards.PhysicalNounCard1E;
import com.gempukku.stccg.cards.PhysicalShipCard;
import com.gempukku.stccg.effects.Effect;
import com.gempukku.stccg.effects.defaulteffect.BeamOrWalkCardsEffect;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.Player;

import java.util.*;

public class WalkCardsAction extends BeamOrWalkAction {

    public WalkCardsAction(Player player, PhysicalNounCard1E cardWalkingFrom) {
        super(player, cardWalkingFrom);
    }

    @Override
    protected Collection<PhysicalCard> getDestinationOptions() {
        if (_cardSource instanceof PhysicalShipCard && ((PhysicalShipCard) _cardSource).isDocked())
            return Collections.singleton(((PhysicalShipCard) _cardSource).getDockedAtCard());
        else if (_cardSource instanceof PhysicalFacilityCard)
            return Filters.filter(_cardSource.getAttachedCards(), _game, Filters.ship, Filters.your(_performingPlayer));
        else return new LinkedList<>();
    }

    @Override
    protected List<PhysicalCard> getValidFromCards() {
        List<PhysicalCard> cards = new ArrayList<>();
        cards.add(_cardSource);
        return cards;
    }

    @Override
    public String getText() { return "Walk"; }

    @Override
    protected Effect finalEffect() {
        return new BeamOrWalkCardsEffect(_cardsToMove, _fromCard, _toCard, _performingPlayerId, getText());
    }

    @Override
    public boolean canBeInitiated() {
            // TODO - No compatibility check
        return !_destinationOptions.isEmpty() &&
                !Filters.filter(_cardSource.getAttachedCards(), Filters.personnel).isEmpty();
    }

}