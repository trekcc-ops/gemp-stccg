package com.gempukku.stccg.actions;

import com.gempukku.stccg.cards.PhysicalNounCard1E;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.filterable.CardType;
import com.gempukku.stccg.effects.Effect;
import com.gempukku.stccg.effects.choose.ChooseCardsOnTableEffect;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gamestate.GameState;
import com.gempukku.stccg.modifiers.ModifiersQuerying;
import com.gempukku.stccg.filters.Filter;
import com.gempukku.stccg.filters.Filters;
import com.google.common.collect.Iterables;

import java.util.*;

public class BeamCardsAction extends AbstractCostToEffectAction {
    private Collection<PhysicalCard> _cardsToBeam;
    private String _playerId;
    private PhysicalNounCard1E _cardUsingTransporters;
    private PhysicalCard _fromCard, _toCard;
    // TODO - The effects below were TargetingEffects in SWCCG. Do they need to be?
    private Effect _chooseFromCardEffect, _chooseToCardEffect, _chooseCardsToBeamEffect;
    private boolean _fromCardChosen, _toCardChosen, _cardsToBeamChosen;
    private boolean _cardsBeamed;
    private Effect _beamCardsEffect;
    private Action _thisAction;

    /**
     * Creates an action to move cards by beaming.
     *
     * @param playerId              the player
     * @param game                  the game
     * @param cardUsingTransporters the card whose transporters are being used to beam
     */
    public BeamCardsAction(String playerId, ST1EGame game, PhysicalNounCard1E cardUsingTransporters) {
        _performingPlayer = playerId;
        _playerId = playerId;
        _cardUsingTransporters = cardUsingTransporters;
        _thisAction = this;

        final GameState gameState = game.getGameState();
        final ModifiersQuerying modifiersQuerying = game.getModifiersQuerying();


        // Get cards that can be beamed
/*        Filter cardFilter = Filters.and(Filters.your(playerId), Filters.or(Filters.personnel, Filters.equipment),
                Filters.presentWith(cardUsingTransporters));
        Collection<PhysicalCard> cardsWithTransporter = Filters.filterActive(game, cardFilter);
        Collection<PhysicalCard> cardsAtSameLocationNotWithTransporter = Filters.filterActive(
                game,
                Filters.your(playerId),
                Filters.or(Filters.personnel, Filters.equipment),
                Filters.atSameLocationAs(cardUsingTransporters),
                Filters.not(Filters.presentWith(cardUsingTransporters))
        );*/

        // Get potential targets to beam to/from
        Collection<PhysicalCard> destinationOptions = Filters.filterActive(
                game,
                Filters.atLocation(_cardUsingTransporters.getCurrentLocation()),
                Filters.or(
                        Filters.planetLocation,
                        Filters.and(
                                Filters.or(Filters.ship, Filters.facility), // TODO - How does this work with sites?
                                Filters.or(Filters.your(playerId)) // TODO - Add unshielded
                        )
                )
        );

        List<PhysicalCard> validFromCards = new ArrayList<>();
        for (PhysicalCard destinationCard : destinationOptions) {
            if (!gameState.getAttachedCards(destinationCard).isEmpty())
                validFromCards.add(destinationCard);
        }
/*
        // Find all cards that can be beamed using the specified transporters
        Map<PhysicalCard, List<PhysicalCard>> beamableCards = new HashMap<>();
        for (PhysicalCard destinationCard : destinationOptions) {
            List<PhysicalCard> beamableCardsAtDestination = new ArrayList<>();
            if (destinationCard.isLocation())
                beamableCardsAtDestination = Filters.filter(destinationCard.getCardsOnSurface(),
                        Filters.your(playerId),
                        Filters.or(Filters.personnel, Filters.equipment));
            else
                beamableCardsAtDestination = Filters.filter(destinationCard.getCardsAboard(), Filters.your(playerId),
                        Filters.or(Filters.personnel, Filters.equipment));
            beamableCards.put(destinationCard, beamableCardsAtDestination);
        }

        // Remove any destinations if no cards can be beamed to/from there
        for (PhysicalCard destination : beamableCards.keySet()) {
            if (destination == _cardUsingTransporters)

            // TODO - Incomplete logic here

        }


        // Figure out which docking bays any of the cards can transit to
        for (PhysicalCard otherDockingBay : otherDockingBays) {
            for (PhysicalCard cardAtDockingBay : cardsAtDockingBay) {
                // Check if card can move to destination card
                if (Filters.canMoveToUsingDockingBayTransit(cardAtDockingBay, false, 0).accepts(gameState, modifiersQuerying, otherDockingBay)) {
                    validDockingBays.add(otherDockingBay);
                    break;
                }
            }
        }
*/
        // Choose card beaming from
        _chooseFromCardEffect = new ChooseCardsOnTableEffect(game, _thisAction, _playerId, "Choose card to beam from",
                validFromCards) {
            @Override
            protected void cardsSelected(Collection<PhysicalCard> cardSelected) {
                _fromCardChosen = true;
                _fromCard = Iterables.getOnlyElement(cardSelected);

                if (_fromCard != _cardUsingTransporters) {
                    _toCard = _cardUsingTransporters;
                    _toCardChosen = true;
                } else {
                    destinationOptions.remove(_fromCard);

                    // Choose card beaming to
                    _chooseToCardEffect = new ChooseCardsOnTableEffect(game, _thisAction, _playerId, "Choose card to beam to",
                            destinationOptions) {
                        @Override
                        protected void cardsSelected(Collection<PhysicalCard> cardSelected) {
                            _toCard = Iterables.getOnlyElement(cardSelected);
                            _toCardChosen = true;

                            // TODO - No checks here yet to make sure cards can be beamed (compatibility, etc.)
                            List<PhysicalCard> beamableCards = gameState.getAttachedCards(_fromCard);

                            // Choose cards to transit
                            _chooseCardsToBeamEffect = new ChooseCardsOnTableEffect(game, _thisAction, _playerId,
                                    "Choose cards to beam to " + _toCard.getCardLink(), 1,
                                    Integer.MAX_VALUE, beamableCards) {
                                @Override
                                protected void cardsSelected(Collection<PhysicalCard> cards) {
                                    _cardsToBeamChosen = true;
                                    _cardsToBeam = cards;

                                    // Transit cards
// TODO                                    _beamCardsEffect = new BeamCardsEffect(_thisAction, _cardsToBeam, _fromCard, _toCard);
                                }
                            };
                        }

                        ;
                    };
                };
            }
        };
    }

    @Override
    public String getText() { return "Beam"; }

    @Override
    public ActionType getActionType() {
        return ActionType.MOVE_CARDS;
    }

    @Override
    public PhysicalCard getActionAttachedToCard() { return _cardUsingTransporters; }
    @Override
    public PhysicalCard getActionSource() { return _cardUsingTransporters; }

    @Override
    public Effect nextEffect(DefaultGame game) {
//        if (!isAnyCostFailed()) {

        Effect cost = getNextCost();
        if (cost != null)
            return cost;

        if (!_fromCardChosen) {
            _fromCardChosen = true;
            appendTargeting(_chooseFromCardEffect);
            return getNextCost();
        }

        if (!_toCardChosen) {
            _toCardChosen = true;
            appendTargeting(_chooseToCardEffect);
            return getNextCost();
        }

        if (!_cardsToBeamChosen) {
            _cardsToBeamChosen = true;
            appendTargeting(_chooseCardsToBeamEffect);
            return getNextCost();
        }

        if (!_cardsBeamed) {
            _cardsBeamed = true;
            return _beamCardsEffect;
        }

        return getNextEffect();
    }

    public boolean wasActionCarriedOut() {
        return _cardsBeamed;
    }

}
