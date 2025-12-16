package com.gempukku.stccg.actions.targetresolver;

import com.gempukku.stccg.actions.choose.*;
import com.gempukku.stccg.cards.physicalcard.*;
import com.gempukku.stccg.common.filterable.FacilityType;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.ST1EGame;

import java.util.*;

public class DownloadMultipleCardsResolver implements ActionTargetResolver {
    private FacilityCard _destinationFacility;
    private final String _performingPlayerName;
    private SelectCardAction _selectDestinationAction;
    private boolean _isFailed;
    private Collection<PhysicalCard> _cardsToDownload;
    private SelectCardsAction _selectCardsToDownloadAction;
    private final Map<PersonnelCard, List<PersonnelCard>> _validCombinations;
    private final int _maxCardCount;

    public DownloadMultipleCardsResolver(Map<PersonnelCard, List<PersonnelCard>> validCombinations,
                                         int maxCardCount,
                                         String performingPlayerName) {
        _validCombinations = validCombinations;
        _maxCardCount = maxCardCount;
        _performingPlayerName = performingPlayerName;
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
            for (PhysicalCard card : Filters.yourFacilitiesInPlay(cardGame, _performingPlayerName)) {
                if (card instanceof FacilityCard facilityCard &&
                        facilityCard.getFacilityType() == FacilityType.OUTPOST) {
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
                    _validCombinations, _maxCardCount);
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

}