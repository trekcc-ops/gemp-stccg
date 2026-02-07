package com.gempukku.stccg.actions.targetresolver;

import com.gempukku.stccg.actions.choose.SelectAffiliationAction;
import com.gempukku.stccg.actions.choose.SelectCardsAction;
import com.gempukku.stccg.actions.choose.SelectVisibleCardsAction;
import com.gempukku.stccg.cards.physicalcard.*;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.FacilityType;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gamestate.GameLocation;
import com.google.common.collect.Iterables;

import java.util.*;

public class ReportCardResolver implements ActionCardResolver {

    private final ReportableCard _cardEnteringPlay;
    private CardWithCrew _destinationCard;
    private Affiliation _affiliationToReportAs;
    private final String _performingPlayerName;
    private SelectCardsAction _selectDestinationAction;
    private boolean _isFailed;
    private SelectAffiliationAction _affiliationSelectionAction;
    private Collection<FacilityCard> _specifiedDestinations;

    public ReportCardResolver(ReportableCard cardEnteringPlay) {
        _cardEnteringPlay = cardEnteringPlay;
        _performingPlayerName = cardEnteringPlay.getOwnerName();
    }

    public ReportCardResolver(ReportableCard cardEnteringPlay, CardWithCrew destination) {
        this(cardEnteringPlay);
        _destinationCard = destination;
    }

    public ReportCardResolver(ReportableCard cardEnteringPlay,
                              Collection<FacilityCard> eligibleDestinations) {
        this(cardEnteringPlay);
        _specifiedDestinations = eligibleDestinations;
    }

    @Override
    public void resolve(DefaultGame cardGame) throws InvalidGameLogicException {
        if (cardGame instanceof ST1EGame stGame) {
            if (!_isFailed && _destinationCard == null) {
                selectDestination(stGame);
            } else if (!_isFailed && _cardEnteringPlay instanceof AffiliatedCard affiliatedCard &&
                    _affiliationToReportAs == null) {
                selectAffiliation(affiliatedCard, stGame);
            }
        } else {
            cardGame.sendErrorMessage("Cannot select cards to report in a non-1E game");
            _isFailed = true;
        }
    }

    private void selectDestination(ST1EGame stGame) {
        if (_selectDestinationAction == null) {
            Collection<FacilityCard> destinationOptions;
            destinationOptions =
                    Objects.requireNonNullElseGet(_specifiedDestinations, () -> getDestinationOptions(stGame));
            _selectDestinationAction = new SelectVisibleCardsAction(stGame, _performingPlayerName,
                    "Choose a facility to report " + _cardEnteringPlay.getCardLink() + " to",
                    Filters.inCards(destinationOptions), 1, 1);
            stGame.addActionToStack(_selectDestinationAction);
        } else if (_selectDestinationAction.wasCompleted()) {
            Collection<PhysicalCard> cardResult = _selectDestinationAction.getSelectedCards();
            if (cardResult.size() == 1 && Iterables.getOnlyElement(cardResult) instanceof FacilityCard facility) {
                _destinationCard = facility;
            } else {
                _isFailed = true;
            }
        } else if (_selectDestinationAction.wasFailed()) {
            _isFailed = true;
        }
    }

    private void selectAffiliation(AffiliatedCard affiliatedCard, ST1EGame stGame) {
        if (affiliatedCard.getAffiliationOptions().size() == 1) {
            _affiliationToReportAs = Iterables.getOnlyElement(affiliatedCard.getAffiliationOptions());
        } else if (_affiliationSelectionAction == null) {
            Set<Affiliation> affiliationOptions = new HashSet<>();
            for (Affiliation affiliation : affiliatedCard.getAffiliationOptions()) {
                if (affiliatedCard.canReportToCrewAsAffiliation(_destinationCard, affiliation, stGame))
                    affiliationOptions.add(affiliation);
            }
            if (affiliationOptions.size() == 1) {
                _affiliationToReportAs = Iterables.getOnlyElement(affiliationOptions);
            } else if (!affiliationOptions.isEmpty()) {
                _affiliationSelectionAction = new SelectAffiliationAction(stGame, _performingPlayerName,
                affiliationOptions);
                stGame.addActionToStack(_affiliationSelectionAction);
            } else {
                _isFailed = true;
            }
        } else if (_affiliationSelectionAction.wasSuccessful()) {
            _affiliationToReportAs = _affiliationSelectionAction.getSelectedAffiliation();
        } else if (_affiliationSelectionAction.wasFailed()) {
            _isFailed = true;
        }
    }

    private Collection<FacilityCard> getDestinationOptions(ST1EGame game) {
        Collection<FacilityCard> availableFacilities = new HashSet<>();
        for (GameLocation location : game.getGameState().getOrderedSpacelineLocations()) {
            Collection<PhysicalCard> facilities =
                    Filters.filterCardsInPlay(game, FacilityType.OUTPOST, Filters.atLocation(location));
            for (PhysicalCard card : facilities) {
                if (card instanceof FacilityCard facility && _cardEnteringPlay.canReportToFacility(facility, game))
                    availableFacilities.add(facility);
            }
        }
        return availableFacilities;
    }


    @Override
    public boolean isResolved() {
        return _destinationCard != null &&
                (_affiliationToReportAs != null || !(_cardEnteringPlay instanceof AffiliatedCard));
    }

    @Override
    public Collection<PhysicalCard> getCards() {
        return List.of(_destinationCard);
    }

    @Override
    public boolean cannotBeResolved(DefaultGame cardGame) {
        if (cardGame instanceof ST1EGame stGame) {
            return _isFailed || getDestinationOptions(stGame).isEmpty();
        } else {
            return true; // Cannot perform report card action in non-1E game
        }
    }

    public CardWithCrew getDestination() {
        return _destinationCard;
    }

    public Affiliation getAffiliationToReportAs() {
        return _affiliationToReportAs;
    }

    public void setDestination(FacilityCard facility) {
        _destinationCard = facility;
    }

    public void setAffiliation(Affiliation affiliation) {
        _affiliationToReportAs = affiliation;
    }
}