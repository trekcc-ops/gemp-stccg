package com.gempukku.stccg.actions.placecard;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.*;
import com.gempukku.stccg.actions.targetresolver.ActionCardResolver;
import com.gempukku.stccg.actions.targetresolver.AllCardsMatchingFilterResolver;
import com.gempukku.stccg.cards.cardgroup.CardPile;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.filters.CardFilter;
import com.gempukku.stccg.game.DefaultGame;
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
                                          String performingPlayerName, ActionCardResolver cardTarget) {
        super(cardGame, performingPlayerName, ActionType.SHUFFLE_CARDS_INTO_DRAW_DECK);
        _cardTarget = cardTarget;
        _performingCard = performingCard;
        _cardTargets.add(_cardTarget);
    }

    public ShuffleCardsIntoDrawDeckAction(DefaultGame cardGame, PhysicalCard performingCard,
                                          String performingPlayerName, CardFilter cardFilter) {
        this(cardGame, performingCard, performingPlayerName, new AllCardsMatchingFilterResolver(cardFilter));
    }

    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        return !_cardTarget.cannotBeResolved(cardGame);
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