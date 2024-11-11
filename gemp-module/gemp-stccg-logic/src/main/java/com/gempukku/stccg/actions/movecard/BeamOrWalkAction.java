package com.gempukku.stccg.actions.movecard;

import com.gempukku.stccg.TextUtils;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.SubAction;
import com.gempukku.stccg.actions.choose.ChooseCardsOnTableEffect;
import com.gempukku.stccg.actions.choose.SelectCardInPlayAction;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalNounCard1E;
import com.gempukku.stccg.cards.physicalcard.PhysicalReportableCard1E;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.ST1EGame;
import com.google.common.collect.Iterables;
import org.apache.commons.lang.StringUtils;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public abstract class BeamOrWalkAction extends ActionyAction {
    private final Collection<PhysicalReportableCard1E> _cardsToMove = new LinkedList<>();
    final PhysicalNounCard1E _cardSource;
    private PhysicalCard _fromCard, _toCard;
    private boolean _fromCardChosen, _toCardChosen, _cardsToMoveChosen, _cardsMoved;
    final Player _performingPlayer;
    final Collection<PhysicalCard> _destinationOptions;

    /**
     * Creates an action to move cards by beaming or walking.
     *
     * @param player              the player
     * @param cardSource        either the card whose transporters are being used, or the card walking from
     */
    BeamOrWalkAction(Player player, PhysicalNounCard1E cardSource) {
        super(player.getPlayerId(), ActionType.MOVE_CARDS);
        _performingPlayer = player;
        _cardSource = cardSource;
        _destinationOptions = getDestinationOptions(cardSource.getGame());
    }

    protected abstract String actionVerb();

    public String getText(DefaultGame game) {
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

    private Effect getChooseCardsToMoveEffect() {
        // TODO - No checks here yet to make sure cards can be moved (compatibility, etc.)
        Collection<PhysicalCard> movableCards =
                Filters.filter(_fromCard.getAttachedCards(_fromCard.getGame()),
                        Filters.your(_performingPlayer), Filters.or(Filters.personnel, Filters.equipment));

        // Choose cards to transit
        return new ChooseCardsOnTableEffect(this, _performingPlayer,
                "Choose cards to " + actionVerb() + " to " + _toCard.getCardLink(), 1,
                movableCards.size(), movableCards) {
            @Override
            protected void cardsSelected(Collection<PhysicalCard> cards) {
                try {
                    for (PhysicalCard card : cards) {
                        if (card instanceof PhysicalReportableCard1E reportable)
                            _cardsToMove.add(reportable);
                        else throw new InvalidGameLogicException("Tried to beam cards that couldn't be reported");
                    }
                } catch(InvalidGameLogicException exp) {
                    _game.sendErrorMessage(exp);
                    _cardsToMove.clear();
                }
                setCardsToMove(_cardsToMove);
            }
        };
    }

    private Effect getChooseToCardEffect() {
        // Choose card beaming to
        return new ChooseCardsOnTableEffect(this, _performingPlayer,
                "Choose card to " + actionVerb() + " to", _destinationOptions) {
            @Override
            protected void cardsSelected(Collection<PhysicalCard> cardSelected) {
                _toCard = Iterables.getOnlyElement(cardSelected);
                _toCardChosen = true;
            }

        };
    }

    private Action selectOriginAction(DefaultGame game) {
        return new SelectCardInPlayAction(this, _performingPlayer,
                "Choose card to " + actionVerb() + " from", getValidFromCards(game)) {

            @Override
            protected void cardSelected(PhysicalCard selectedCard) {
                _fromCardChosen = true;
                _fromCard = selectedCard;

                if (_fromCard == _cardSource) {
                    _destinationOptions.removeIf(card -> card == _fromCard);
                } else {
                    _destinationOptions.clear();
                    _destinationOptions.add(_cardSource);
                }
            }
        };
    }


    @Override
    public Action nextAction(DefaultGame cardGame) {
//        if (!isAnyCostFailed()) {

        Action cost = getNextCost();
        if (cost != null)
            return cost;

        if (!_fromCardChosen) {
            Action action = selectOriginAction(cardGame);
            appendTargeting(action);
            return getNextCost();
        }

        if (!_toCardChosen) {
            Effect effect = getChooseToCardEffect();
            appendTargeting(new SubAction(this, effect));
            return getNextCost();
        }

        if (!_cardsToMoveChosen) {
            Effect effect = getChooseCardsToMoveEffect();
            appendTargeting(new SubAction(this, effect));
            return getNextCost();
        }

        if (!_cardsMoved) {
            for (PhysicalReportableCard1E card : _cardsToMove) {
                cardGame.getGameState().transferCard(card, _toCard); // attach card to destination card
                card.setLocation(_toCard.getLocation());
                if (_fromCard instanceof MissionCard)
                    card.leaveAwayTeam();
                if (_toCard instanceof MissionCard mission)
                    card.joinEligibleAwayTeam(mission);
            }
            if (!_cardsToMove.isEmpty()) {
                cardGame.sendMessage(_performingPlayerId + " " + actionVerb() + "ed " +
                        TextUtils.plural(_cardsToMove.size(), "card") + " from " +
                        _fromCard.getCardLink() + " to " + _toCard.getCardLink());
            }
            _cardsMoved = true;
        }

        return getNextAction();
    }

    public void setCardsToMove(Collection<? extends PhysicalReportableCard1E> cards) {
        _cardsToMove.addAll(cards);
        _cardsToMoveChosen = true;
    }

    public void setDestination(PhysicalCard destination) {
        _toCard = destination;
        _toCardChosen = true;
    }

    public void setOrigin(PhysicalCard origin) {
        _fromCard = origin;
        _fromCardChosen = true;
    }

}