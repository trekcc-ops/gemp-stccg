package com.gempukku.stccg.actions.movecard;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalNounCard1E;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.ST1EGame;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BeamCardsAction extends BeamOrWalkAction {

    public BeamCardsAction(Player player, PhysicalNounCard1E cardUsingTransporters) {
        super(player, cardUsingTransporters);
    }

    @Override
    protected Collection<PhysicalCard> getDestinationOptions(ST1EGame game) {
            // Includes your ships and facilities at card source's location, as well as planet locations at card source's location
        return Filters.filterActive(
                game,
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
    protected List<PhysicalCard> getValidFromCards(DefaultGame game) {
            // Destination options filtered to remove cards with none of your personnel or equipment aboard
        List<PhysicalCard> cards = new ArrayList<>();
        for (PhysicalCard destinationCard : _destinationOptions) {
            if (!Filters.filter(destinationCard.getAttachedCards(game),
                    Filters.your(_performingPlayer), Filters.or(Filters.equipment, Filters.personnel)).isEmpty())
                // TODO - Doesn't do a compatibility or beamable check, does it need to?
                cards.add(destinationCard);
        }
        return cards;
    }

    protected String actionVerb() { return "beam"; }

    public boolean requirementsAreMet(DefaultGame cardGame) {
        return (!getValidFromCards(cardGame).isEmpty());
    }

    public PhysicalCard getCardUsingTransporters() { return _cardSource; }

}