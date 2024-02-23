package com.gempukku.stccg.actions;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.cards.PhysicalNounCard1E;
import com.gempukku.stccg.effects.Effect;
import com.gempukku.stccg.effects.choose.ChooseCardsOnTableEffect;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gamestate.GameState;
import com.gempukku.stccg.modifiers.ModifiersQuerying;
import com.google.common.collect.Iterables;

import java.util.Collection;
import java.util.List;

public abstract class BeamOrWalkAction extends AbstractCostToEffectAction {
    protected Collection<PhysicalCard> _cardsToMove;
    protected PhysicalNounCard1E _cardSource;
    protected PhysicalCard _fromCard, _toCard;
    // TODO - The effects below were TargetingEffects in SWCCG. Do they need to be?
    private Effect _chooseFromCardEffect, _chooseToCardEffect, _chooseCardsToMoveEffect;
    private boolean _fromCardChosen, _toCardChosen, _cardsToMoveChosen;
    private boolean _cardsBeamed;
    private Action _thisAction;
    protected final ST1EGame _game;
    protected final Player _performingPlayer;
    Collection<PhysicalCard> _destinationOptions;
    List<PhysicalCard> _validFromCards;

    /**
     * Creates an action to move cards by beaming.
     *
     * @param playerId              the player
     * @param game                  the game
     * @param cardUsingTransporters the card whose transporters are being used to beam
     */
    public BeamOrWalkAction(Player player, PhysicalNounCard1E cardSource) {
        _game = cardSource.getGame();
        _performingPlayer = player;
        _performingPlayerId = player.getPlayerId();
        _cardSource = cardSource;
        _thisAction = this;

        final GameState gameState = _game.getGameState();
        final ModifiersQuerying modifiersQuerying = _game.getModifiersQuerying();

        // Get potential targets to beam to/from
        _destinationOptions = getDestinationOptions();
        _validFromCards = getValidFromCards();
        String verb = getText().toLowerCase();

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
                                    _cardsToMove = cards;
                                }
                            };
                        }

                    };
                }
        };
    }

    @Override
    public ActionType getActionType() {
        return ActionType.MOVE_CARDS;
    }

    @Override
    public PhysicalCard getActionAttachedToCard() { return _cardSource; }
    @Override
    public PhysicalCard getActionSource() { return _cardSource; }
    protected abstract Collection<PhysicalCard> getDestinationOptions();
    protected abstract List<PhysicalCard> getValidFromCards();

    @Override
    public Effect nextEffect() {
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

    public boolean wasActionCarriedOut() {
        return _cardsBeamed;
    }

    @Override
    public ST1EGame getGame() { return _game; }

}
