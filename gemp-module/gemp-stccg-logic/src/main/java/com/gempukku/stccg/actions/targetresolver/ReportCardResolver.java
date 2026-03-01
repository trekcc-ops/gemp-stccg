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

import java.util.*;

public class ReportCardResolver implements ActionCardResolver {

    private ReportableCard _cardEnteringPlay;
    private PhysicalCard _destinationCard;
    private Affiliation _affiliationToReportAs;
    private final String _performingPlayerName;
    private SelectCardsAction _selectDestinationAction;
    private SelectCardsAction _selectCardToPlayAction;
    private boolean _isFailed;
    private SelectAffiliationAction _affiliationSelectionAction;
    private final Map<PhysicalCard, Map<PhysicalCard, List<Affiliation>>> _targetMap = new HashMap<>();

    public ReportCardResolver(DefaultGame cardGame, ReportableCard cardEnteringPlay) {
        _cardEnteringPlay = cardEnteringPlay;
        _performingPlayerName = cardEnteringPlay.getOwnerName();
        Map<PhysicalCard, List<Affiliation>> destinationAndAffiliationMap = ((ST1EGame) cardGame).getRules()
                .getDestinationAndAffiliationMapForReportingCard(cardEnteringPlay, (ST1EGame) cardGame,
                        false);
        if (!destinationAndAffiliationMap.isEmpty()) {
            _targetMap.put(cardEnteringPlay, destinationAndAffiliationMap);
        }
    }

    public ReportCardResolver(ReportableCard cardEnteringPlay, Map<PhysicalCard, List<Affiliation>> calculatedDestinationMap) {
        _cardEnteringPlay = cardEnteringPlay;
        _performingPlayerName = cardEnteringPlay.getOwnerName();
        if (!calculatedDestinationMap.isEmpty()) {
            _targetMap.put(cardEnteringPlay, calculatedDestinationMap);
        }
    }

    public ReportCardResolver(String performingPlayerName,
                              Map<PhysicalCard, Map<PhysicalCard, List<Affiliation>>> targetMap) {
        _performingPlayerName = performingPlayerName;
        _targetMap.putAll(targetMap);
    }

    @Override
    public void resolve(DefaultGame cardGame) throws InvalidGameLogicException {
        if (cardGame instanceof ST1EGame stGame) {
            if (!_isFailed && _cardEnteringPlay == null) {
                selectCardEnteringPlay(stGame);
            } else if (!_isFailed && _destinationCard == null) {
                selectDestination(stGame);
            } else if (!_isFailed && _cardEnteringPlay instanceof AffiliatedCard &&
                    _affiliationToReportAs == null) {
                selectAffiliation(stGame);
            }
        } else {
            cardGame.sendErrorMessage("Cannot select cards to report in a non-1E game");
            _isFailed = true;
        }
    }

    private void selectCardEnteringPlay(ST1EGame stGame) {
        if (_selectCardToPlayAction == null) {
            Collection<PhysicalCard> options = _targetMap.keySet();
            _selectCardToPlayAction = new SelectVisibleCardsAction(stGame, _performingPlayerName,
                    "Choose a card to report",
                    Filters.inCards(options), 1, 1);
            stGame.addActionToStack(_selectCardToPlayAction);
        } else if (_selectCardToPlayAction.wasCompleted()) {
            Collection<PhysicalCard> cardResult = _selectCardToPlayAction.getSelectedCards();
            if (cardResult.size() == 1 && Iterables.getOnlyElement(cardResult) instanceof ReportableCard reportable) {
                _cardEnteringPlay = reportable;
            } else {
                _isFailed = true;
            }
        } else if (_selectCardToPlayAction.wasFailed()) {
            _isFailed = true;
        }
    }

    private void selectDestination(ST1EGame stGame) {
        if (_selectDestinationAction == null) {
            Collection<PhysicalCard> destinationOptions = _targetMap.get(_cardEnteringPlay).keySet();
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

    private void selectAffiliation(ST1EGame stGame) {
        Collection<Affiliation> affiliationOptions = _targetMap.get(_cardEnteringPlay).get(_destinationCard);
        if (affiliationOptions.size() == 1) {
            _affiliationToReportAs = Iterables.getOnlyElement(affiliationOptions);
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
        return _isFailed || (!(cardGame instanceof ST1EGame)) ||
                _targetMap.isEmpty();
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

    public ReportableCard getCardEnteringPlay() {
        return _cardEnteringPlay;
    }

    public Collection<? extends PhysicalCard> getSelectableCardsToEnterPlay() {
        if (_cardEnteringPlay != null) {
            return List.of(_cardEnteringPlay);
        } else {
            return _targetMap.keySet();
        }
    }

    public void selectCardToPlay(ReportableCard cardToPlay) {
        _cardEnteringPlay = cardToPlay;
    }
}