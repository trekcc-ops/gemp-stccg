package com.gempukku.stccg.actions.movecard;

import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalNounCard1E;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.player.Player;

import java.util.*;

public class NewBeamCardsAction extends BeamOrWalkAction {

    public List<Map<String, Object>> getActionInitiationMatrix(DefaultGame cardGame) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (PhysicalCard transportersCard : getCardsWithTransporters(cardGame)) {
            for (PhysicalCard originCard : getValidOrigins(cardGame, transportersCard)) {
                for (PhysicalCard destinationCard : getValidDestinations(cardGame, transportersCard, originCard)) {
                    List<PhysicalCard> beamableCards = getBeamableCards(cardGame, originCard);
                    if (!beamableCards.isEmpty()) {
                        List<Integer> beamableCardIds = new ArrayList<>();
                        for (PhysicalCard beamableCard : beamableCards) {
                            beamableCardIds.add(beamableCard.getCardId());
                        }
                        Map<String, Object> option = new HashMap<>();
                        option.put("TRANSPORTERS_CARD", transportersCard.getCardId());
                        option.put("ORIGIN_CARD", originCard.getCardId());
                        option.put("DESTINATION_CARD", destinationCard.getCardId());
                        option.put("BEAMABLE_CARDS", beamableCardIds);
                        result.add(option);
                    }
                }
            }
        }
        return result;
    }

    private List<PhysicalCard> getCardsWithTransporters(DefaultGame cardGame) {
        List<PhysicalCard> result = new ArrayList<>();
        for (PhysicalCard card : cardGame.getGameState().getAllCardsInGame()) {
            if (card instanceof PhysicalNounCard1E nounCard) {
                if (nounCard.isControlledBy(_performingPlayer) && nounCard.hasTransporters()) {
                    result.add(nounCard);
                }
            }
        }
        return result;
    }

    private List<PhysicalCard> getValidOrigins(DefaultGame cardGame, PhysicalCard transportersCard) {
        List<PhysicalCard> result = new ArrayList<>();
        result.add(transportersCard);
        Collection<PhysicalCard> otherCards = Filters.filterCardsInPlay(cardGame,
                Filters.atLocation(transportersCard.getGameLocation()),
                Filters.or(
                        Filters.planetLocation,
                        Filters.and(
                                Filters.or(Filters.ship, Filters.facility),
                                Filters.or(Filters.your(_performingPlayerId))
                        )
                )
        );
        result.addAll(otherCards);
        return result;
    }

    private List<PhysicalCard> getValidDestinations(DefaultGame cardGame, PhysicalCard transportersCard, PhysicalCard origin) {
        List<PhysicalCard> result = new ArrayList<>();
        if (transportersCard == origin) {
            List<PhysicalCard> destinations = getValidOrigins(cardGame, transportersCard);
            destinations.remove(transportersCard);
            result.addAll(destinations);
        } else {
            result.add(transportersCard);
        }
        return result;
    }

    private List<PhysicalCard> getBeamableCards(DefaultGame cardGame, PhysicalCard origin) {
        Collection<PhysicalCard> movableCards =
                Filters.filter(origin.getAttachedCards(cardGame), cardGame,
                        Filters.your(_performingPlayerId), Filters.or(Filters.personnel, Filters.equipment));
        return new ArrayList<>(movableCards);
    }

    public NewBeamCardsAction(DefaultGame cardGame, Player player, PhysicalNounCard1E cardUsingTransporters) {
        super(cardGame, player, cardUsingTransporters, ActionType.BEAM_CARDS);
    }

    @Override
    protected Collection<PhysicalCard> getDestinationOptions(ST1EGame game) {
        // Includes your ships and facilities at card source's location, as well as planet locations at card source's location
        return Filters.filterCardsInPlay(
                game,
                Filters.atLocation(_cardSource.getGameLocation()),
                Filters.or(
                        Filters.planetLocation,
                        Filters.and(
                                Filters.or(Filters.ship, Filters.facility), // TODO - How does this work with sites?
                                Filters.or(Filters.your(_performingPlayerId)) // TODO - Add unshielded
                        )
                )
        );
    }

    @Override
    public List<PhysicalCard> getValidFromCards(DefaultGame game) {
            // Destination options filtered to remove cards with none of your personnel or equipment aboard
        List<PhysicalCard> cards = new ArrayList<>();
        for (PhysicalCard destinationCard : _destinationOptions) {
            if (!Filters.filter(destinationCard.getAttachedCards(game), game,
                    Filters.your(_performingPlayerId), Filters.or(Filters.equipment, Filters.personnel)).isEmpty())
                // TODO - Doesn't do a compatibility or beamable check, does it need to?
                cards.add(destinationCard);
        }
        return cards;
    }

    protected String actionVerb() { return "beam"; }

    public boolean requirementsAreMet(DefaultGame cardGame) {
        List<PhysicalCard> fromCards = getValidFromCards(cardGame);
        List<PhysicalCard> toCards = new LinkedList<>(getDestinationOptions((ST1EGame) cardGame));
        if (fromCards.isEmpty() || toCards.isEmpty())
            return false;
        return fromCards.size() != 1 || toCards.size() != 1 || fromCards.getFirst() != toCards.getFirst();
    }

    public PhysicalCard getCardUsingTransporters() { return _cardSource; }

}