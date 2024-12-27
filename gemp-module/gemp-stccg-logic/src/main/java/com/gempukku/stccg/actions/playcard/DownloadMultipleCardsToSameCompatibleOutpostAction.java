package com.gempukku.stccg.actions.playcard;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.actions.choose.SelectVisibleCardAction;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.FacilityType;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.decisions.ArbitraryCardsSelectionDecision;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.modifiers.ModifierFlag;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class DownloadMultipleCardsToSameCompatibleOutpostAction extends ActionyAction
        implements TopLevelSelectableAction {
    private final String _playerId;
    private final int _maxCardCount;
    private List<Action> _playCardActions = new LinkedList<>();
    private final Zone _fromZone;
    private final DefaultGame _game;
    private final PhysicalCard _actionSource;
    private final Map<PersonnelCard, List<PersonnelCard>> _validCombinations;
    private FacilityCard _destination;
    private final List<FacilityCard> _destinationOptions = new LinkedList<>();
    private boolean _destinationChosen = false;
    private List<PhysicalCard> _cardsToDownload;
    private boolean _cardsPlayed = false;
    private boolean _cardsToDownloadChosen = false;
    private SelectVisibleCardAction _selectDestinationAction;

    public DownloadMultipleCardsToSameCompatibleOutpostAction(Zone fromZone, Player player, PhysicalCard actionSource,
                                                              Map<PersonnelCard, List<PersonnelCard>> validCombinations,
                                                              int maxCardCount) {
        super(player, "Download card from " + fromZone.getHumanReadable(), ActionType.DOWNLOAD_CARD);
        _playerId = player.getPlayerId();
        _actionSource = actionSource;
        _validCombinations = validCombinations;
        _fromZone = fromZone;
        _game = player.getGame();
        _maxCardCount = maxCardCount;
    }

    protected Collection<PhysicalCard> getPlayableCards() {
        return new LinkedList<>(_validCombinations.keySet());
    }

    @Override
    public boolean wasCarriedOut() {
        if (_playCardActions.isEmpty())
            return false;
        for (Action action : _playCardActions) {
            if (!action.wasCarriedOut()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        if (_fromZone == Zone.DISCARD || _fromZone == Zone.DRAW_DECK)
            return !_game.getModifiersQuerying().hasFlagActive(ModifierFlag.CANT_PLAY_FROM_DISCARD_OR_DECK) &&
                    !getPlayableCards().isEmpty();
        else
            return !getPlayableCards().isEmpty();
    }

    @Override
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException {
        Player performingPlayer = cardGame.getPlayer(_playerId);
        Action thisAction = this;

        if (!_cardsToDownloadChosen) {
            cardGame.getUserFeedback().sendAwaitingDecision(
                    new ArbitraryCardsSelectionDecision(performingPlayer, "Choose card(s) to download",
                            new LinkedList<>(getPlayableCards()), _validCombinations, 0, _maxCardCount) {
                        @Override
                        public void decisionMade(String result) throws DecisionResultInvalidException {
                            _cardsToDownloadChosen = true;
                            _cardsToDownload = getSelectedCardsByResponse(result);
                            if (!_cardsToDownload.isEmpty()) {

                                for (PhysicalCard card : Filters.yourFacilitiesInPlay(performingPlayer)) {
                                    if (card instanceof FacilityCard facilityCard &&
                                            facilityCard.getFacilityType() == FacilityType.OUTPOST) {
                                        boolean allCompatible = true;
                                        for (PhysicalCard selectedCard : _cardsToDownload) {
                                            if (!((PersonnelCard) selectedCard).isCompatibleWith(facilityCard)) {
                                                allCompatible = false;
                                            }
                                        }
                                        if (allCompatible)
                                            _destinationOptions.add(facilityCard);
                                    }
                                }

                                if (_destinationOptions.isEmpty()) {
                                    _destinationChosen = true;
                                    _cardsPlayed = true;
                                    throw new DecisionResultInvalidException("Could find no compatible outpost to " +
                                            "download cards to");
                                } else if (_destinationOptions.size() == 1) {
                                    _destinationChosen = true;
                                    _destination = _destinationOptions.getFirst();
                                }
                            }
                        }
                    });
        }

        if (!_destinationChosen) {
            if (_selectDestinationAction == null) {
                _selectDestinationAction = new SelectVisibleCardAction(thisAction, performingPlayer,
                        "Select outpost to download cards to", _destinationOptions);
                return _selectDestinationAction;
            } else if (_selectDestinationAction.wasCarriedOut()) {
                _destinationChosen = true;
                _destination = (FacilityCard) _selectDestinationAction.getSelectedCard();
            } else {
                throw new InvalidGameLogicException("Unable to identify destination to download cards to");
            }
        }

        if (!_cardsPlayed) {
            for (PhysicalCard card : _cardsToDownload) {
                Action playCardAction = card.getPlayCardAction(true);
                if (playCardAction instanceof ReportCardAction reportAction) {
                    reportAction.setDestination(_destination);
                    _game.getActionsEnvironment().addActionToStack(playCardAction);
                    _playCardActions.add(playCardAction);
                }
            }
        }

        return null;
    }

    @Override
    public PhysicalCard getPerformingCard() {
        return _actionSource;
    }

    @Override
    public PhysicalCard getCardForActionSelection() {
        return _actionSource;
    }

    public String getPerformingPlayerId() { return _playerId; }

    public DefaultGame getGame() { return _game; }
}