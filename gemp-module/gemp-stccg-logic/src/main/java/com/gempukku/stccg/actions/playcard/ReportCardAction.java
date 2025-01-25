package com.gempukku.stccg.actions.playcard;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.choose.SelectAffiliationAction;
import com.gempukku.stccg.actions.choose.SelectVisibleCardsAction;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalReportableCard1E;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.FacilityType;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.gamestate.MissionLocation;
import com.gempukku.stccg.gamestate.ST1EGameState;
import com.google.common.collect.Iterables;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class ReportCardAction extends STCCGPlayCardAction {
    private FacilityCard _reportingDestination;
    private boolean _cardPlayed;
    private boolean _destinationOptionsIdentified;
    private Collection<PhysicalCard> _destinationOptions;
    private boolean _destinationChosen = false;
    private final Set<Affiliation> _affiliationOptions = new HashSet<>();
    private boolean _affiliationWasChosen;
    private Affiliation _selectedAffiliation;
    private final PhysicalReportableCard1E _cardEnteringPlay;
    private SelectAffiliationAction _selectAffiliationAction;
    private SelectVisibleCardsAction _selectDestinationAction;

    public ReportCardAction(PhysicalReportableCard1E cardToPlay, boolean forFree) {
                    // TODO - Zone is null because these will be attached and the implementation is weird
        super(cardToPlay, null, cardToPlay.getOwner(), forFree);
        _cardEnteringPlay = cardToPlay;
        setText("Play " + _cardEnteringPlay.getFullName());
        if (cardToPlay.isMultiAffiliation()) {
            _affiliationWasChosen = false;
        } else {
            _affiliationWasChosen = true;
            _selectedAffiliation = cardToPlay.getCurrentAffiliation();
        }
    }

    public ReportCardAction(PhysicalReportableCard1E cardToPlay, boolean forFree,
                            Collection<PhysicalCard> destinationOptions) {
        this(cardToPlay, forFree);
        _destinationOptionsIdentified = true;
        _destinationOptions = destinationOptions;

    }


    protected Collection<PhysicalCard> getDestinationOptions(DefaultGame game) throws InvalidGameLogicException {
        if (game.getGameState() instanceof ST1EGameState gameState) {
            if (_destinationOptionsIdentified)
                return _destinationOptions;
            else {
                Collection<PhysicalCard> availableFacilities = new HashSet<>();
                for (MissionLocation location : gameState.getSpacelineLocations()) {
                    Collection<PhysicalCard> facilities =
                            Filters.filterActive(game, FacilityType.OUTPOST, Filters.atLocation(location));
                    for (PhysicalCard card : facilities) {
                        if (card instanceof FacilityCard facility && _cardEnteringPlay.canReportToFacility(facility))
                            availableFacilities.add(facility);
                    }
                }
                return availableFacilities;
            }
        } else {
            throw new InvalidGameLogicException("Tried to process a report card action in a non-1E game");
        }
    }

    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        boolean result;
        try {
            Collection<PhysicalCard> destinationOptions = getDestinationOptions(cardGame);
            result = _cardEnteringPlay.canBePlayed(cardGame) && !destinationOptions.isEmpty();
        } catch(InvalidGameLogicException exp) {
            cardGame.sendErrorMessage(exp);
            result = false;
        }
        return costsCanBePaid(cardGame) && result;
    }

    @Override
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException {

        if (isCostFailed())
            return null;

        Player performingPlayer = cardGame.getPlayer(_performingPlayerId);

        if (!_destinationChosen) {
            if (_selectDestinationAction == null) {
                _selectDestinationAction = new SelectVisibleCardsAction(performingPlayer,
                        "Choose a facility to report " + _cardEnteringPlay.getCardLink() + " to",
                        Filters.inCards(getDestinationOptions(cardGame)), 1, 1);
                return _selectDestinationAction;
            } else if (_selectDestinationAction.wasCarriedOut()) {
                try {
                    PhysicalCard result = Iterables.getOnlyElement(_selectDestinationAction.getSelectedCards());
                    if (result instanceof FacilityCard facility) {
                        _reportingDestination = facility;
                        _destinationChosen = true;
                        if (!_affiliationWasChosen) {
                            for (Affiliation affiliation : _cardEnteringPlay.getAffiliationOptions()) {
                                if (_cardEnteringPlay.canReportToFacilityAsAffiliation(
                                        _reportingDestination, affiliation))
                                    _affiliationOptions.add(affiliation);
                            }
                            if (_affiliationOptions.size() == 1) {
                                _affiliationWasChosen = true;
                                _selectedAffiliation = Iterables.getOnlyElement(_affiliationOptions);
                            }
                        }
                    } else throw new InvalidGameLogicException("Expected a facility card from selection action");
                } catch (IllegalArgumentException exp) {
                    throw new InvalidGameLogicException(exp.getMessage());
                }
            }
        }

        Action nextCost = getNextCost();
        if (nextCost != null)
            return nextCost;

        if (!_affiliationWasChosen) {
            if (_affiliationOptions.size() > 1) {
                if (_selectAffiliationAction == null) {
                    _selectAffiliationAction =
                            new SelectAffiliationAction(performingPlayer, _affiliationOptions);
                    return _selectAffiliationAction;
                } else if (_selectAffiliationAction.wasCarriedOut()) {
                    _selectedAffiliation = _selectAffiliationAction.getSelectedAffiliation();
                    _affiliationWasChosen = true;
                }
            } else {
                _selectedAffiliation = Iterables.getOnlyElement(_affiliationOptions);
                _affiliationWasChosen = true;
            }
        }
        if (!_cardPlayed) {
            _cardEnteringPlay.changeAffiliation(_selectedAffiliation);
            _cardPlayed = true;

            cardGame.sendMessage(_cardEnteringPlay.getOwnerName() + " played " + _cardEnteringPlay.getCardLink());

            _cardEnteringPlay.reportToFacility(_reportingDestination);
            performingPlayer.addPlayedAffiliation(_cardEnteringPlay.getCurrentAffiliation());
            cardGame.getActionsEnvironment().emitEffectResult(
                    new PlayCardResult(this, _fromZone, _cardEnteringPlay));
        }

        return getNextAction();
    }

    public PhysicalCard getCardReporting() { return _cardEnteringPlay; }

    public void setDestination(FacilityCard card) {
        _destinationChosen = true;
        _reportingDestination = card;
    }

}