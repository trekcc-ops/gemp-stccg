package com.gempukku.stccg.actions.playcard;

import com.gempukku.stccg.actions.*;
import com.gempukku.stccg.actions.choose.SelectAffiliationAction;
import com.gempukku.stccg.actions.choose.SelectCardsAction;
import com.gempukku.stccg.actions.choose.SelectVisibleCardsAction;
import com.gempukku.stccg.cards.physicalcard.*;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.FacilityType;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gamestate.GameState;
import com.gempukku.stccg.gamestate.MissionLocation;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;
import com.google.common.collect.Iterables;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ReportCardAction extends STCCGPlayCardAction {
    private ActionCardResolver _destinationTarget;
    private AffiliationResolver _affiliationTarget;

    private enum Progress { destinationOptionsIdentified, destinationSelected, affiliationSelected, cardPlayed }

    public ReportCardAction(DefaultGame cardGame, ReportableCard cardToPlay, boolean forFree) {
        // TODO - Zone is null because these will be attached and the implementation is weird
        super(cardGame, cardToPlay, null, cardToPlay.getOwnerName(), forFree, Progress.values());
        if (cardToPlay instanceof AffiliatedCard affiliatedCard) {
            Set<Affiliation> affiliationOptions = affiliatedCard.getAffiliationOptions();
            if (affiliationOptions.size() == 1) {
                setProgress(Progress.affiliationSelected);
                _affiliationTarget = new AffiliationResolver(Iterables.getOnlyElement(affiliationOptions));
            }
        } else {
            setProgress(Progress.affiliationSelected);
        }
    }

    public ReportCardAction(DefaultGame cardGame, ReportableCard cardToPlay, boolean forFree,
                            Collection<PhysicalCard> destinationOptions) {
        this(cardGame, cardToPlay, forFree);
        setProgress(Progress.destinationOptionsIdentified);
        SelectCardsAction selectDestinationAction = new SelectVisibleCardsAction(cardGame,
                cardToPlay.getOwnerName(),
                "Choose a facility to report " + _cardEnteringPlay.getCardLink() + " to",
                Filters.inCards(destinationOptions), 1, 1);
        _destinationTarget = new SelectCardsResolver(selectDestinationAction);
    }

    public ReportCardAction(DefaultGame cardGame, ReportableCard cardToPlay, boolean forFree,
                            FacilityCard facilityCard) {
        this(cardGame, cardToPlay, forFree);
        setProgress(Progress.destinationOptionsIdentified);
        setProgress(Progress.destinationSelected);
        _destinationTarget = new FixedCardResolver(facilityCard);
    }



    protected Collection<PhysicalCard> getDestinationOptions(DefaultGame game) throws InvalidGameLogicException {
        if (_cardEnteringPlay instanceof ReportableCard reportable) {
            if (game instanceof ST1EGame stGame) {
                Collection<PhysicalCard> availableFacilities = new HashSet<>();
                for (MissionLocation location : stGame.getGameState().getSpacelineLocations()) {
                    Collection<PhysicalCard> facilities =
                            Filters.filterCardsInPlay(game, FacilityType.OUTPOST, Filters.atLocation(location));
                    for (PhysicalCard card : facilities) {
                        if (card instanceof FacilityCard facility && reportable.canReportToFacility(facility, stGame))
                            availableFacilities.add(facility);
                    }
                }
                return availableFacilities;
            } else {
                throw new InvalidGameLogicException("Tried to process a report card action in a non-1E game");
            }
        } else {
            throw new InvalidGameLogicException("Tried to report a non-1E card");
        }
    }

    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        boolean result;
        try {
            if (getProgress(Progress.destinationOptionsIdentified)) {
                result = _cardEnteringPlay.canBePlayed(cardGame);
            } else {
                Collection<PhysicalCard> destinationOptions = getDestinationOptions(cardGame);
                result = _cardEnteringPlay.canBePlayed(cardGame) && !destinationOptions.isEmpty();
            }
        } catch(InvalidGameLogicException exp) {
            cardGame.sendErrorMessage(exp);
            result = false;
        }
        return costsCanBePaid(cardGame) && result;
    }

    @Override
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException, PlayerNotFoundException {
        if (_cardEnteringPlay instanceof ReportableCard reportable) {

            if (isCostFailed())
                return null;

            if (!getProgress(Progress.destinationSelected)) {
                if (_destinationTarget == null) {
                    SelectCardsAction selectDestinationAction = new SelectVisibleCardsAction(cardGame, _performingPlayerId,
                            "Choose a facility to report " + _cardEnteringPlay.getCardLink() + " to",
                            Filters.inCards(getDestinationOptions(cardGame)), 1, 1);
                    _destinationTarget = new SelectCardsResolver(selectDestinationAction);
                    return selectDestinationAction;
                } else if (!_destinationTarget.isResolved()) {
                    if (_destinationTarget instanceof SelectCardsResolver resolver && !resolver.getSelectionAction().wasCarriedOut()) {
                        return resolver.getSelectionAction();
                    } else {
                        _destinationTarget.resolve(cardGame);
                        try {
                            PhysicalCard result = Iterables.getOnlyElement(_destinationTarget.getCards(cardGame));
                            if (result instanceof FacilityCard) {
                                setProgress(Progress.destinationSelected);
                            } else
                                throw new InvalidGameLogicException("Cards for report card action did not match expected classes");
                        } catch (IllegalArgumentException exp) {
                            throw new InvalidGameLogicException(exp.getMessage());
                        }
                    }
                } else {
                    try {
                        PhysicalCard result = Iterables.getOnlyElement(_destinationTarget.getCards(cardGame));
                        if (result instanceof FacilityCard) {
                            setProgress(Progress.destinationSelected);
                        } else
                            throw new InvalidGameLogicException("Cards for report card action did not match expected classes");
                    } catch (IllegalArgumentException exp) {
                        throw new InvalidGameLogicException(exp.getMessage());
                    }
                }
            }

            Action nextCost = getNextCost();
            if (nextCost != null)
                return nextCost;

            if (_cardEnteringPlay instanceof AffiliatedCard affiliatedCard && _affiliationTarget == null) {
                PhysicalCard result = Iterables.getOnlyElement(_destinationTarget.getCards(cardGame));
                FacilityCard facility = (FacilityCard) result;
                Set<Affiliation> affiliationOptions = new HashSet<>();
                for (Affiliation affiliation : affiliatedCard.getAffiliationOptions()) {
                    if (affiliatedCard.canReportToFacilityAsAffiliation(facility, affiliation, (ST1EGame) cardGame))
                        affiliationOptions.add(affiliation);
                }
                if (affiliationOptions.size() == 1) {
                    setProgress(Progress.affiliationSelected);
                    _affiliationTarget = new AffiliationResolver(Iterables.getOnlyElement(affiliationOptions));
                } else if (!affiliationOptions.isEmpty()) {
                    _affiliationTarget = new AffiliationResolver(new SelectAffiliationAction(_cardEnteringPlay,
                            cardGame, _performingPlayerId, affiliationOptions));
                } else {
                    setAsFailed();
                    throw new InvalidGameLogicException("Unable to report card. No valid affiliations to report as.");
                }
            }

            if (!getProgress(Progress.affiliationSelected)) {
                if (_affiliationTarget.isResolved()) {
                    setProgress(Progress.affiliationSelected);
                } else if (_affiliationTarget.getSelectionAction().wasCarriedOut()) {
                    setProgress(Progress.affiliationSelected);
                    _affiliationTarget.resolve();
                } else {
                    return _affiliationTarget.getSelectionAction();
                }
            }

            if (!getProgress(Progress.cardPlayed)) {
                processEffect(reportable, cardGame);
            }

            return getNextAction();
        } else {
            throw new InvalidGameLogicException("Tried to report an invalid class type of card");
        }
    }

    public void processEffect(ReportableCard reportable, DefaultGame cardGame)
            throws InvalidGameLogicException, PlayerNotFoundException {
        Player performingPlayer = cardGame.getPlayer(_performingPlayerId);
        if (reportable instanceof AffiliatedCard affiliatedCard) {
            Affiliation chosenAffiliation = _affiliationTarget.getAffiliation();
            affiliatedCard.changeAffiliation((ST1EGame) cardGame, chosenAffiliation);
            performingPlayer.addPlayedAffiliation(chosenAffiliation);
        }
        setProgress(Progress.cardPlayed);
        setAsSuccessful();

        FacilityCard facility = getSelectedDestination(cardGame);
        GameState gameState = cardGame.getGameState();

        gameState.removeCardsFromZoneWithoutSendingToClient(cardGame, Collections.singleton(reportable));
        reportable.setLocation(cardGame, facility.getGameLocation());
        reportable.attachTo(facility);
        gameState.addCardToZone(cardGame, reportable, Zone.ATTACHED, _actionContext);

        if (reportable instanceof ShipCard ship) {
            ship.dockAtFacility(facility);
        }

        saveResult(new PlayCardResult(this, _cardEnteringPlay));
    }

    public void setDestination(FacilityCard card) {
        setProgress(Progress.destinationSelected);
        _destinationTarget = new FixedCardResolver(card);
    }

    @Override
    public boolean wasCarriedOut() {
        return getProgress(Progress.cardPlayed);
    }

    private FacilityCard getSelectedDestination(DefaultGame cardGame) throws InvalidGameLogicException {

        Collection<PhysicalCard> result = _destinationTarget.getCards(cardGame);
        if (result.size() != 1) {
            throw new InvalidGameLogicException("Unable to identify reporting destination");
        } else {
            PhysicalCard destination = Iterables.getOnlyElement(result);
            if (destination instanceof FacilityCard facility) {
                return facility;
            } else {
                throw new InvalidGameLogicException("Unable to report to a non-facility card");
            }
        }
    }

}