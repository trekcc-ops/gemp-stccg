package com.gempukku.stccg.actions.targetresolver;

import com.gempukku.stccg.actions.choose.SelectAffiliationAction;
import com.gempukku.stccg.actions.choose.SelectCardsAction;
import com.gempukku.stccg.actions.choose.SelectVisibleCardsAction;
import com.gempukku.stccg.cards.physicalcard.AffiliatedCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.ReportableCard;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.ST1EGame;
import com.google.common.collect.Iterables;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ReportCardResolver implements ActionCardResolver {

    private final ReportableCard _cardEnteringPlay;
    private PhysicalCard _destinationCard;
    private Affiliation _affiliationToReportAs;
    private final String _performingPlayerName;
    private SelectCardsAction _selectDestinationAction;
    private boolean _isFailed;
    private SelectAffiliationAction _affiliationSelectionAction;
    private final Map<PhysicalCard, List<Affiliation>> _destinationAndAffiliationMap;

    public ReportCardResolver(DefaultGame cardGame, ReportableCard cardEnteringPlay) {
        _cardEnteringPlay = cardEnteringPlay;
        _performingPlayerName = cardEnteringPlay.getOwnerName();
        _destinationAndAffiliationMap = ((ST1EGame) cardGame).getRules()
                .getDestinationAndAffiliationMapForReportingCard(cardEnteringPlay, (ST1EGame) cardGame,
                        false);
    }


    public ReportCardResolver(DefaultGame cardGame, ReportableCard cardEnteringPlay,
                              Collection<PhysicalCard> eligibleDestinations, boolean specialReporting) {
        _cardEnteringPlay = cardEnteringPlay;
        _performingPlayerName = cardEnteringPlay.getOwnerName();
        _destinationAndAffiliationMap = ((ST1EGame) cardGame).getRules()
                .getDestinationAndAffiliationMapForReportingCard(cardEnteringPlay, (ST1EGame) cardGame,
                        eligibleDestinations, specialReporting);
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
            Collection<PhysicalCard> destinationOptions = _destinationAndAffiliationMap.keySet();
            _selectDestinationAction = new SelectVisibleCardsAction(stGame, _performingPlayerName,
                    "Choose a card to report " + _cardEnteringPlay.getCardLink() + " to",
                    Filters.inCards(destinationOptions), 1, 1);
            stGame.addActionToStack(_selectDestinationAction);
        } else if (_selectDestinationAction.wasCompleted()) {
            Collection<PhysicalCard> cardResult = _selectDestinationAction.getSelectedCards();
            if (cardResult.size() == 1) {
                _destinationCard = Iterables.getOnlyElement(cardResult);
            } else {
                _isFailed = true;
            }
        } else if (_selectDestinationAction.wasFailed()) {
            _isFailed = true;
        }
    }

    private void selectAffiliation(AffiliatedCard affiliatedCard, ST1EGame stGame) {
        Collection<Affiliation> affiliationOptions = _destinationAndAffiliationMap.get(_destinationCard);
        if (affiliationOptions.size() == 1) {
            _affiliationToReportAs = Iterables.getOnlyElement(affiliatedCard.getAffiliationOptions());
        } else if (affiliationOptions.isEmpty()) {
            _isFailed = true;
        } else if (_affiliationSelectionAction == null) {
            _affiliationSelectionAction = new SelectAffiliationAction(stGame, _performingPlayerName,
                    affiliationOptions);
            stGame.addActionToStack(_affiliationSelectionAction);
        } else if (_affiliationSelectionAction.wasSuccessful()) {
            _affiliationToReportAs = _affiliationSelectionAction.getSelectedAffiliation();
        } else if (_affiliationSelectionAction.wasFailed()) {
            _isFailed = true;
        }
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
        return _isFailed || (!(cardGame instanceof ST1EGame));
    }

    public PhysicalCard getDestination() {
        return _destinationCard;
    }

    public Affiliation getAffiliationToReportAs() {
        return _affiliationToReportAs;
    }

    public void setDestination(PhysicalCard destination) {
        _destinationCard = destination;
    }

    public void setAffiliation(Affiliation affiliation) {
        _affiliationToReportAs = affiliation;
    }
}