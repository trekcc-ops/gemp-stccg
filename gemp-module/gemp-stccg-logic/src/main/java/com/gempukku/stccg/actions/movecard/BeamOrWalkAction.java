package com.gempukku.stccg.actions.movecard;

import com.gempukku.stccg.TextUtils;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.actions.choose.SelectCardInPlayAction;
import com.gempukku.stccg.actions.choose.SelectCardsOnTableAction;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalNounCard1E;
import com.gempukku.stccg.cards.physicalcard.PhysicalReportableCard1E;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.ST1EGame;
import com.google.common.collect.Iterables;
import org.apache.commons.lang.StringUtils;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public abstract class BeamOrWalkAction extends ActionyAction {
    private final Collection<PhysicalCard> _cardsToMove = new LinkedList<>();
    final PhysicalNounCard1E _cardSource;
    private PhysicalCard _origin, _destination;
    private boolean _fromCardChosen, _toCardChosen, _cardsToMoveChosen;
    final Player _performingPlayer;
    final Collection<PhysicalCard> _destinationOptions;
    private SelectCardInPlayAction _selectOriginAction;
    private SelectCardInPlayAction _selectDestinationAction;
    private SelectCardsOnTableAction _selectCardsToMoveAction;

    /**
     * Creates an action to move cards by beaming or walking.
     *
     * @param player              the player
     * @param cardSource        either the card whose transporters are being used, or the card walking from
     */
    BeamOrWalkAction(Player player, PhysicalNounCard1E cardSource) {
        super(player, ActionType.MOVE_CARDS);
        _performingPlayer = player;
        _cardSource = cardSource;
        _destinationOptions = getDestinationOptions(cardSource.getGame());
    }

    protected abstract String actionVerb();

    @Override
    public String getActionSelectionText(DefaultGame game) {
        List<PhysicalCard> validFromCards = getValidFromCards(game);
        StringBuilder sb = new StringBuilder();
        sb.append(StringUtils.capitalize(actionVerb())).append(" cards");
        List<PhysicalCard> destinations = _destinationOptions.stream().toList();
        if (destinations.size() == 1 && destinations.getFirst() != _cardSource) {
            sb.append(" to ").append(Iterables.getOnlyElement(destinations).getTitle());
        }
        else if (validFromCards.size() == 1 && validFromCards.getFirst() != _cardSource)
            sb.append(" from ").append(Iterables.getOnlyElement(validFromCards).getTitle());
        return sb.toString();
    }

    @Override
    public PhysicalCard getCardForActionSelection() { return _cardSource; }
    @Override
    public PhysicalCard getActionSource() { return _cardSource; }
    protected abstract Collection<PhysicalCard> getDestinationOptions(ST1EGame game);
    public abstract List<PhysicalCard> getValidFromCards(DefaultGame game);


    @Override
    public Action nextAction(DefaultGame cardGame) {
//        if (!isAnyCostFailed()) {

        Action cost = getNextCost();
        if (cost != null)
            return cost;

        if (!_fromCardChosen) {
            if (_selectOriginAction == null) {
                _selectOriginAction = new SelectCardInPlayAction(this, _performingPlayer,
                        "Choose card to " + actionVerb() + " from", getValidFromCards(cardGame));
                appendTargeting(_selectOriginAction);
                return getNextCost();
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
        }

        if (!_toCardChosen) {
            if (_selectDestinationAction == null) {
                _selectDestinationAction = new SelectCardInPlayAction(this, _performingPlayer,
                        "Choose card to " + actionVerb() + " to ", _destinationOptions);
                appendTargeting(_selectDestinationAction);
                return getNextCost();
            } else {
                if (_selectDestinationAction.wasCarriedOut())
                    setDestination(_selectDestinationAction.getSelectedCard());
            }
        }

        if (!_cardsToMoveChosen) {
            if (_selectCardsToMoveAction == null) {
                // TODO - No checks here yet to make sure cards can be moved (compatibility, etc.)
                Collection<PhysicalCard> movableCards =
                        Filters.filter(_origin.getAttachedCards(_origin.getGame()),
                                Filters.your(_performingPlayer), Filters.or(Filters.personnel, Filters.equipment));
                _selectCardsToMoveAction = new SelectCardsOnTableAction(this, _performingPlayer,
                        "Choose cards to " + actionVerb() + " to " + _destination.getCardLink(),
                        movableCards, 1);
                appendTargeting(_selectCardsToMoveAction);
                return getNextCost();
            } else {
                if (_selectCardsToMoveAction.wasCarriedOut())
                    setCardsToMove(_selectCardsToMoveAction.getSelectedCards());
            }
        }

        if (!_wasCarriedOut) {
            for (PhysicalCard card : _cardsToMove) {
                cardGame.getGameState().transferCard(card, _destination); // attach card to destination card
                card.setLocation(_destination.getLocation());
                if (_origin instanceof MissionCard)
                    ((PhysicalReportableCard1E) card).leaveAwayTeam();
                if (_destination instanceof MissionCard mission)
                    ((PhysicalReportableCard1E) card).joinEligibleAwayTeam(mission.getLocation());
            }
            if (!_cardsToMove.isEmpty()) {
                cardGame.sendMessage(_performingPlayerId + " " + actionVerb() + "ed " +
                        TextUtils.plural(_cardsToMove.size(), "card") + " from " +
                        _origin.getCardLink() + " to " + _destination.getCardLink());
            }
            _wasCarriedOut = true;
        }

        return getNextAction();
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