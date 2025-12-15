package com.gempukku.stccg.actions.movecard;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.actions.choose.SelectVisibleCardAction;
import com.gempukku.stccg.actions.choose.SelectVisibleCardsAction;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.ReportableCard;
import com.gempukku.stccg.cards.physicalcard.ST1EPhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gamestate.GameLocation;
import com.gempukku.stccg.gamestate.MissionLocation;
import com.gempukku.stccg.player.Player;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public abstract class BeamOrWalkAction extends ActionyAction implements TopLevelSelectableAction {

    @JsonProperty("targetCardIds")
    @JsonIdentityReference(alwaysAsId=true)
    private final Collection<PhysicalCard> _cardsToMove = new LinkedList<>();

    @JsonProperty("performingCardId")
    @JsonIdentityReference(alwaysAsId=true)
    final ST1EPhysicalCard _cardSource;

    @JsonProperty("originCardId")
    private PhysicalCard _origin;

    @JsonProperty("destinationCardId")
    private PhysicalCard _destination;
    private boolean _fromCardChosen, _toCardChosen, _cardsToMoveChosen;
    final Collection<PhysicalCard> _destinationOptions;
    private SelectVisibleCardAction _selectOriginAction;
    private SelectVisibleCardAction _selectDestinationAction;
    private SelectVisibleCardsAction _selectCardsToMoveAction;

    /**
     * Creates an action to move cards by beaming or walking.
     *
     * @param player              the player
     * @param cardSource        either the card whose transporters are being used, or the card walking from
     */
    BeamOrWalkAction(DefaultGame cardGame, Player player, ST1EPhysicalCard cardSource, ActionType actionType) {
        super(cardGame, player, actionType);
        _cardSource = cardSource;
        _destinationOptions = getDestinationOptions((ST1EGame) cardGame);
    }

    protected abstract String actionVerb();

    @Override
    public PhysicalCard getPerformingCard() { return _cardSource; }
    protected abstract Collection<PhysicalCard> getDestinationOptions(ST1EGame game);
    public abstract List<PhysicalCard> getValidFromCards(DefaultGame game);

    protected void continueInitiation(DefaultGame cardGame) throws InvalidGameLogicException {
        if (!_fromCardChosen) {
            if (_selectOriginAction == null) {
                _selectOriginAction = new SelectVisibleCardAction(cardGame, _performingPlayerId,
                        "Choose card to " + actionVerb() + " from", getValidFromCards(cardGame));
                cardGame.addActionToStack(_selectOriginAction);
            } else {
                if (_selectOriginAction.wasCarriedOut()) {
                    _origin = _selectOriginAction.getSelectedCard();
                    _fromCardChosen = true;
                    if (_origin == _cardSource) {
                        _destinationOptions.removeIf(card -> card == _origin);
                    } else {
                        _destinationOptions.clear();
                        _destinationOptions.add(_cardSource);
                    }
                }
            }
        } else if (_destinationOptions.isEmpty()) {
            throw new InvalidGameLogicException("Unable to locate a valid destination");
        } else if (!_toCardChosen) {
            if (_selectDestinationAction == null) {
                _selectDestinationAction = new SelectVisibleCardAction(cardGame, _performingPlayerId,
                        "Choose card to " + actionVerb() + " to ", _destinationOptions);
                cardGame.addActionToStack(_selectDestinationAction);
            } else {
                if (_selectDestinationAction.wasCarriedOut())
                    setDestination(_selectDestinationAction.getSelectedCard());
            }
        } else if (!_cardsToMoveChosen) {
            if (_selectCardsToMoveAction == null) {
                // TODO - No checks here yet to make sure cards can be moved (compatibility, etc.)
                Collection<PhysicalCard> movableCards =
                        Filters.filter(_origin.getAttachedCards(cardGame), cardGame,
                                Filters.your(_performingPlayerId), Filters.or(Filters.personnel, Filters.equipment));
                _selectCardsToMoveAction = new SelectVisibleCardsAction(cardGame, _performingPlayerId,
                        "Choose cards to " + actionVerb() + " to " + _destination.getCardLink(),
                        movableCards, 1);
                cardGame.addActionToStack(_selectCardsToMoveAction);
            } else {
                if (_selectCardsToMoveAction.wasCarriedOut())
                    setCardsToMove(_selectCardsToMoveAction.getSelectedCards());
            }
        } else {
            super.continueInitiation(cardGame);
        }
    }

    protected void processEffect(DefaultGame cardGame) {
        if (isInProgress()) {
            GameLocation destinationLocation = _destination.getGameLocation((ST1EGame) cardGame);
            for (PhysicalCard card : _cardsToMove) {
                card.setZone(Zone.ATTACHED);
                card.attachTo(_destination);
                card.setLocationId(cardGame, _destination.getLocationId());
                if (_origin instanceof MissionCard) {
                    ((ST1EGame) cardGame).getGameState().removeCardFromAwayTeam((ST1EGame) cardGame, (ReportableCard) card);
                }
                if (_destination instanceof MissionCard && destinationLocation instanceof MissionLocation mission) {
                    ((ST1EGame) cardGame).getGameState().addCardToEligibleAwayTeam((ST1EGame) cardGame, (ReportableCard) card, mission);
                }
            }
            setAsSuccessful();
        }
    }

    public void setCardsToMove(Collection<? extends PhysicalCard> cards) {
        _cardsToMove.clear();
        _cardsToMove.addAll(cards);
        _cardsToMoveChosen = true;
    }

    public void setDestination(PhysicalCard destination) {
        _destination = destination;
        _toCardChosen = true;
    }

    public void setOrigin(PhysicalCard origin) {
        _origin = origin;
        _fromCardChosen = true;
    }

}