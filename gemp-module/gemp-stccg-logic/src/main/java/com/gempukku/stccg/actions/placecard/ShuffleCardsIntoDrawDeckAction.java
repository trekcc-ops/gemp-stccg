package com.gempukku.stccg.actions.placecard;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.*;
import com.gempukku.stccg.cards.cardgroup.CardPile;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.filters.CardFilter;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;

import java.util.Collection;

public class ShuffleCardsIntoDrawDeckAction extends ActionyAction implements TopLevelSelectableAction {
    @JsonIdentityReference(alwaysAsId=true)
    @JsonProperty("performingCardId")
    private final PhysicalCard _performingCard;
    @JsonProperty("cardTarget")
    private final ActionCardResolver _cardTarget;
    @JsonProperty("targetCardIds")
    @JsonIdentityReference(alwaysAsId=true)
    private Collection<PhysicalCard> _targetCards;

    public ShuffleCardsIntoDrawDeckAction(DefaultGame cardGame, PhysicalCard performingCard,
                                          String performingPlayerName, CardFilter cardFilter) {
        super(cardGame, performingPlayerName, "Shuffle cards into draw deck",
                ActionType.SHUFFLE_CARDS_INTO_DRAW_DECK);
        _cardTarget = new AllCardsMatchingFilterResolver(cardFilter);
        _performingCard = performingCard;
    }

    public ShuffleCardsIntoDrawDeckAction(DefaultGame cardGame, PhysicalCard performingCard,
                                          String performingPlayerName, ActionCardResolver cardTarget) {
        super(cardGame, performingPlayerName, "Shuffle cards into draw deck",
                ActionType.SHUFFLE_CARDS_INTO_DRAW_DECK);
        _cardTarget = cardTarget;
        _performingCard = performingCard;
    }


    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        return !_cardTarget.cannotBeResolved(cardGame);
    }

    @Override
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException, PlayerNotFoundException {
        if (!_cardTarget.isResolved()) {
            Action selectionAction = _cardTarget.getSelectionAction();
            if (selectionAction != null && !selectionAction.wasCarriedOut()) {
                return selectionAction;
            } else {
                _cardTarget.resolve(cardGame);
            }
        }

        Action nextAction = getNextAction();
        if (nextAction == null)
            processEffect(cardGame);
        return nextAction;
    }

    @Override
    public PhysicalCard getPerformingCard() {
        return _performingCard;
    }

    public void processEffect(DefaultGame cardGame) {
        _targetCards = _cardTarget.getCards(cardGame);
        cardGame.getGameState().removeCardsFromZoneWithoutSendingToClient(cardGame, _targetCards);
        for (PhysicalCard card : _targetCards) {
            cardGame.getGameState().addCardToTopOfDrawDeck(card);
        }
        try {
            Player performingPlayer = cardGame.getPlayer(_performingPlayerId);
            CardPile<PhysicalCard> drawDeck = performingPlayer.getDrawDeck();
            drawDeck.shuffle();
            setAsSuccessful();
        } catch(PlayerNotFoundException exp) {
            cardGame.sendErrorMessage(exp);
            setAsFailed();
        }
    }

}