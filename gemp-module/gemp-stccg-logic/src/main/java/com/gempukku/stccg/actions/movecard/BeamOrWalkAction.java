package com.gempukku.stccg.actions.movecard;

import com.gempukku.stccg.actions.AbstractCostToEffectAction;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.choose.ChooseCardsOnTableEffect;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalNounCard1E;
import com.gempukku.stccg.cards.physicalcard.PhysicalReportableCard1E;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gamestate.GameState;
import com.google.common.collect.Iterables;
import org.apache.commons.lang.StringUtils;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public abstract class BeamOrWalkAction extends AbstractCostToEffectAction {
    protected Collection<PhysicalReportableCard1E> _cardsToMove;
    protected final PhysicalNounCard1E _cardSource;
    protected PhysicalCard _fromCard, _toCard;
    // TODO - The effects below were TargetingEffects in Star Wars GEMP. Do they need to be?
    private final Effect _chooseFromCardEffect;
    private Effect _chooseToCardEffect;
    private Effect _chooseCardsToMoveEffect;
    private boolean _fromCardChosen, _toCardChosen, _cardsToMoveChosen;
    private boolean _cardsBeamed;
    protected final ST1EGame _game;
    protected final Player _performingPlayer;
    final Collection<PhysicalCard> _destinationOptions;
    final List<PhysicalCard> _validFromCards;

    /**
     * Creates an action to move cards by beaming or walking.
     *
     * @param player              the player
     * @param cardSource        either the card whose transporters are being used, or the card walking from
     */
    public BeamOrWalkAction(Player player, PhysicalNounCard1E cardSource) {
        super(player.getPlayerId(), ActionType.MOVE_CARDS);
        _game = cardSource.getGame();
        _performingPlayer = player;
        _cardSource = cardSource;

        final GameState gameState = _game.getGameState();

        // Get potential targets to beam to/from
        _destinationOptions = getDestinationOptions();
        _validFromCards = getValidFromCards();
        String verb = getVerb();

        // Choose card beaming from
        _chooseFromCardEffect = new ChooseCardsOnTableEffect(_thisAction, _performingPlayerId,
                "Choose card to " + verb + " from", _validFromCards) {
            @Override
            protected void cardsSelected(Collection<PhysicalCard> cardSelected) {
                _fromCardChosen = true;
                _fromCard = Iterables.getOnlyElement(cardSelected);

                if (_fromCard != _cardSource) {
                    _destinationOptions.clear();
                    _destinationOptions.add(_cardSource);
                } else {
                    _destinationOptions.removeIf(card -> card == _fromCard);
                }

                    // Choose card beaming to
                    _chooseToCardEffect = new ChooseCardsOnTableEffect(_thisAction, _performingPlayerId,
                            "Choose card to " + verb + " to", _destinationOptions) {
                        @Override
                        protected void cardsSelected(Collection<PhysicalCard> cardSelected) {
                            _toCard = Iterables.getOnlyElement(cardSelected);
                            _toCardChosen = true;

                            // TODO - No checks here yet to make sure cards can be moved (compatibility, etc.)
                            Collection<PhysicalCard> movableCards =
                                    Filters.filter(gameState.getAttachedCards(_fromCard), _game, Filters.your(player),
                                            Filters.or(Filters.personnel, Filters.equipment));

                            // Choose cards to transit
                            _chooseCardsToMoveEffect = new ChooseCardsOnTableEffect(_thisAction, _performingPlayerId,
                                    "Choose cards to " + verb + " to " + _toCard.getCardLink(), 1,
                                    Integer.MAX_VALUE, movableCards) {
                                @Override
                                protected void cardsSelected(Collection<PhysicalCard> cards) {
                                    _cardsToMoveChosen = true;
                                    _cardsToMove = new LinkedList<>();
                                    for (PhysicalCard card : cards)
                                        if (card instanceof PhysicalReportableCard1E reportable)
                                            _cardsToMove.add(reportable);
                                }
                            };
                        }

                    };
                }
        };
    }

    protected abstract String getVerb();

    public String getText() {
        StringBuilder sb = new StringBuilder();
        sb.append(StringUtils.capitalize(getVerb())).append(" cards");
        List<PhysicalCard> destinations = _destinationOptions.stream().toList();
        if (destinations.size() == 1 && destinations.getFirst() != _cardSource) {
            sb.append(" to ").append(destinations.getFirst().getTitle());
        }
        else if (_validFromCards.size() == 1 && _validFromCards.getFirst() != _cardSource)
            sb.append(" from ").append(_validFromCards.getFirst().getTitle());
        return sb.toString();
    }

    @Override
    public PhysicalCard getActionAttachedToCard() { return _cardSource; }
    @Override
    public PhysicalCard getActionSource() { return _cardSource; }
    protected abstract Collection<PhysicalCard> getDestinationOptions();
    protected abstract List<PhysicalCard> getValidFromCards();

    @Override
    public Effect nextEffect() throws InvalidGameLogicException {
//        if (!isAnyCostFailed()) {

        Effect cost = getNextCost();
        if (cost != null)
            return cost;

        if (!_fromCardChosen) {
            appendTargeting(_chooseFromCardEffect);
            return getNextCost();
        }

        if (!_toCardChosen) {
            appendTargeting(_chooseToCardEffect);
            return getNextCost();
        }

        if (!_cardsToMoveChosen) {
            appendTargeting(_chooseCardsToMoveEffect);
            return getNextCost();
        }

        if (!_cardsBeamed) {
            _cardsBeamed = true;
            return finalEffect();
        }

        return getNextEffect();
    }

    protected abstract Effect finalEffect();

    @Override
    public ST1EGame getGame() { return _game; }

}
