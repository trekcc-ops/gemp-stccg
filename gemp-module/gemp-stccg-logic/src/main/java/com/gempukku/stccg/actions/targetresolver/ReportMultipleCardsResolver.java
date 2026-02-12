package com.gempukku.stccg.actions.targetresolver;

import com.gempukku.stccg.actions.choose.SelectCardAction;
import com.gempukku.stccg.actions.choose.SelectCardsAction;
import com.gempukku.stccg.actions.choose.SelectValidCardCombinationFromDialogToDownloadAction;
import com.gempukku.stccg.actions.choose.SelectVisibleCardAction;
import com.gempukku.stccg.cards.physicalcard.CardWithCompatibility;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.filters.CardFilter;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.ST1EGame;

import java.util.*;

public class ReportMultipleCardsResolver implements ActionCardResolver {
    private FacilityCard _destinationFacility;
    private final Collection<FacilityCard> _eligibleDestinations;
    private final String _performingPlayerName;
    private SelectCardAction _selectDestinationAction;
    private boolean _isFailed;
    private Collection<PhysicalCard> _cardsToDownload;
    private SelectCardsAction _selectCardsToDownloadAction;
    private final Map<PersonnelCard, List<PersonnelCard>> _validCombinations;
    private final int _maxCardCount;
    private final int _minCardCount;

    public ReportMultipleCardsResolver(CardFilter reportableFilter, CardFilter destinationFilter, int minCount,
                                       int maxCount, boolean differentCardsOnly, boolean specialReporting,
                                       DefaultGame cardGame, String performingPlayerName) {
        _performingPlayerName = performingPlayerName;
        _maxCardCount = maxCount;
        _minCardCount = minCount;
        Collection<PhysicalCard> reportableCards = Filters.filter(cardGame, reportableFilter);
        Collection<FacilityCard> eligibleDestinations = new ArrayList<>();
        for (PhysicalCard card : Filters.filter(cardGame, destinationFilter)) {
            if (card instanceof FacilityCard facility && facility.isInPlay()) {
                eligibleDestinations.add(facility);
            }
        }
        _eligibleDestinations = eligibleDestinations;
        Map<PersonnelCard, List<PersonnelCard>> validCombinations = new HashMap<>();
        for (PhysicalCard reportable1 : reportableCards) {
            if (reportable1 instanceof PersonnelCard personnel1) {
                List<PersonnelCard> compatiblePersonnel = new ArrayList<>();
                for (PhysicalCard reportable2 : reportableCards) {
                    if (reportable2 instanceof PersonnelCard personnel2 && cardGame instanceof ST1EGame stGame &&
                            cardsCanBeReportedTogether(personnel1, personnel2, stGame, differentCardsOnly,
                                    specialReporting)) {
                        compatiblePersonnel.add(personnel2);
                    }
                }
                validCombinations.put(personnel1, compatiblePersonnel);
            }
        }
        _validCombinations = validCombinations;
    }

    private boolean cardsCanBeReportedTogether(PersonnelCard personnel1, PersonnelCard personnel2, ST1EGame stGame,
                                               boolean differentCardsOnly, boolean specialReporting) {
        if (!personnel1.isCompatibleWith(stGame, personnel2)) {
            return false;
        } else if (differentCardsOnly && personnel1.isCopyOf(personnel2)) {
            return false;
        }

        for (FacilityCard facility : _eligibleDestinations) {
            if (specialReporting) {
                if (personnel1.isCompatibleWith(stGame, facility) && personnel2.isCompatibleWith(stGame, facility)) {
                    return true;
                }
            } else {
                Map<PhysicalCard, List<Affiliation>> destinationMap =
                        stGame.getRules().getDestinationAndAffiliationMapForReportingCard(personnel1, stGame, List.of(facility), true);
                Map<PhysicalCard, List<Affiliation>> destinationMap2 =
                        stGame.getRules().getDestinationAndAffiliationMapForReportingCard(personnel2, stGame, List.of(facility), true);
                if (destinationMap.containsKey(facility) && destinationMap2.containsKey(facility)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void resolve(DefaultGame cardGame) throws InvalidGameLogicException {
        if (cardGame instanceof ST1EGame stGame) {
            if (_cardsToDownload == null) {
                selectCardsToDownload(cardGame);
            } else if (_cardsToDownload.isEmpty()) {
                throw new InvalidGameLogicException("Unable to identify any cards to download");
            } else if (_destinationFacility == null) {
                selectDestination(stGame);
            }
        } else {
            cardGame.sendErrorMessage("Unable to resolve download cards target in non-1E game");
            _isFailed = true;
        }
    }

    private void selectDestination(ST1EGame cardGame) {
        if (_selectDestinationAction == null) {
            List<FacilityCard> destinationOptions = new ArrayList<>();
            for (FacilityCard facilityCard : _eligibleDestinations) {
                boolean allCompatible = true;
                for (PhysicalCard selectedCard : _cardsToDownload) {
                    if (selectedCard instanceof CardWithCompatibility stCard) {
                        if (!facilityCard.isCompatibleWith(cardGame, stCard)) {
                            allCompatible = false;
                        }
                    } else {
                        allCompatible = false;
                    }
                }
                if (allCompatible)
                    destinationOptions.add(facilityCard);
            }
            if (destinationOptions.isEmpty()) {
                _isFailed = true;
            } else if (destinationOptions.size() == 1) {
                _destinationFacility = destinationOptions.getFirst();
            } else {
                _selectDestinationAction = new SelectVisibleCardAction(cardGame, _performingPlayerName,
                        "Select outpost to download cards to", destinationOptions);
                cardGame.addActionToStack(_selectDestinationAction);
            }
        } else if (_selectDestinationAction.wasSuccessful()) {
            if (_selectDestinationAction.getSelectedCard() instanceof FacilityCard facility) {
                _destinationFacility = facility;
            } else {
                _isFailed = true;
            }
        } else if (_selectDestinationAction.wasFailed()) {
            _isFailed = true;
        }
    }

    private void selectCardsToDownload(DefaultGame cardGame) {
        if (_selectCardsToDownloadAction == null) {
            _selectCardsToDownloadAction = new SelectValidCardCombinationFromDialogToDownloadAction(cardGame,
                    _performingPlayerName, _validCombinations.keySet(),
                    _validCombinations, _minCardCount, _maxCardCount);
            cardGame.addActionToStack(_selectCardsToDownloadAction);
        } else if (_selectCardsToDownloadAction.wasFailed()) {
            _isFailed = true;
        } else if (_selectCardsToDownloadAction.wasSuccessful()) {
            _cardsToDownload = _selectCardsToDownloadAction.getSelectedCards();
        }
    }


    @Override
    public boolean isResolved() {
        return _cardsToDownload != null && _destinationFacility != null;
    }

    public Collection<PhysicalCard> getCardsToDownload() {
        return _cardsToDownload;
    }

    @Override
    public boolean cannotBeResolved(DefaultGame cardGame) {
        return _isFailed;
    }

    public FacilityCard getDestinationFacility() {
        return _destinationFacility;
    }

    public void setDestination(FacilityCard facility) {
        _destinationFacility = facility;
    }

    @Override
    public Collection<PhysicalCard> getCards() {
        return _cardsToDownload;
    }

}