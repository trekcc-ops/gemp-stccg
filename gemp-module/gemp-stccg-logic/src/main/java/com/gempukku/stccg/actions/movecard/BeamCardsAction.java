package com.gempukku.stccg.actions.movecard;

import com.gempukku.stccg.cards.physicalcard.PhysicalNounCard1E;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.actions.Effect;

import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.filters.Filters;

import java.util.*;

public class BeamCardsAction extends BeamOrWalkAction {

    public BeamCardsAction(Player player, PhysicalNounCard1E cardUsingTransporters) {
        super(player, cardUsingTransporters);
    }

    @Override
    protected Collection<PhysicalCard> getDestinationOptions() {
            // Includes your ships and facilities at card source's location, as well as planet locations at card source's location
        return Filters.filterActive(
                _game,
                Filters.atLocation(_cardSource.getLocation()),
                Filters.or(
                        Filters.planetLocation,
                        Filters.and(
                                Filters.or(Filters.ship, Filters.facility), // TODO - How does this work with sites?
                                Filters.or(Filters.your(_performingPlayer)) // TODO - Add unshielded
                        )
                )
        );
    }

    @Override
    protected List<PhysicalCard> getValidFromCards() {
            // Destination options filtered to remove cards with none of your personnel or equipment aboard
        List<PhysicalCard> cards = new ArrayList<>();
        for (PhysicalCard destinationCard : _destinationOptions) {
            if (!Filters.filter(_game.getGameState().getAttachedCards(destinationCard), _game,
                    Filters.your(_performingPlayer), Filters.or(Filters.equipment, Filters.personnel)).isEmpty())
                // TODO - Doesn't do a compatibility or beamable check, does it need to?
                cards.add(destinationCard);
        }
        return cards;
    }

    protected String getVerb() { return "beam"; }

    @Override
    protected Effect finalEffect() {
        return new BeamOrWalkCardsEffect(_cardsToMove, _fromCard, _toCard, _performingPlayerId, "Beam");
    }
    @Override
    public boolean canBeInitiated() {
        return (!_validFromCards.isEmpty());
    }

}