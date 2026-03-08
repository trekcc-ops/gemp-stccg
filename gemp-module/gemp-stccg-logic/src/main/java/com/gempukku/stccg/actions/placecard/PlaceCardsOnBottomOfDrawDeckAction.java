package com.gempukku.stccg.actions.placecard;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.*;
import com.gempukku.stccg.actions.choose.SelectCardsAction;
import com.gempukku.stccg.actions.targetresolver.ActionCardResolver;
import com.gempukku.stccg.actions.targetresolver.FixedCardsResolver;
import com.gempukku.stccg.actions.targetresolver.SelectCardsResolver;
import com.gempukku.stccg.cards.cardgroup.DrawDeck;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.gamestate.GameState;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;

import java.util.Collection;

public class PlaceCardsOnBottomOfDrawDeckAction extends ActionyAction {

    private final ActionCardResolver _resolver;
    private Collection<PhysicalCard> _cardsToPlace;
    private final boolean _showOpponent;

    public PlaceCardsOnBottomOfDrawDeckAction(DefaultGame cardGame, String performingPlayerName,
                                              ActionCardResolver resolver, boolean showOpponent) {
        super(cardGame, performingPlayerName, ActionType.PLACE_CARDS_BENEATH_DRAW_DECK);
        _resolver = resolver;
        _cardTargets.add(_resolver);
        _showOpponent = showOpponent;
    }


    public PlaceCardsOnBottomOfDrawDeckAction(DefaultGame cardGame, String performingPlayerName,
                                              SelectCardsAction selectionAction, boolean showOpponent) {
        this(cardGame, performingPlayerName, new SelectCardsResolver(selectionAction), showOpponent);
    }


    public PlaceCardsOnBottomOfDrawDeckAction(DefaultGame cardGame, Player performingPlayer,
                                              SelectCardsAction selectionAction, boolean showOpponent) {
        this(cardGame, performingPlayer.getPlayerId(), new SelectCardsResolver(selectionAction), showOpponent);
    }


    public PlaceCardsOnBottomOfDrawDeckAction(DefaultGame cardGame, String performingPlayerName,
                                              Collection<PhysicalCard> cardsToPlace, boolean showOpponent) {
        this(cardGame, performingPlayerName, new FixedCardsResolver(cardsToPlace), showOpponent);
    }


    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        return true;
    }

    @Override
    protected void processEffect(DefaultGame cardGame) {
        try {
            _cardsToPlace = _resolver.getCards(cardGame);
            GameState gameState = cardGame.getGameState();
            gameState.removeCardsFromZoneWithoutSendingToClient(cardGame, _cardsToPlace);

            for (PhysicalCard card : _cardsToPlace) {
                Player cardOwner = cardGame.getPlayer(card.getOwnerName());
                DrawDeck drawDeck = cardOwner.getDrawDeck();
                drawDeck.addCardToBottom(card);
                card.setZone(Zone.DRAW_DECK);
                if (_showOpponent) {
                    card.reveal();
                }
            }
            saveResult(new PlaceCardInDrawDeckResult(cardGame, this, PlaceCardInDrawDeckResult.Placement.BOTTOM,
                _cardsToPlace, _showOpponent), cardGame);
            setAsSuccessful();
        } catch(PlayerNotFoundException exp) {
            cardGame.sendErrorMessage(exp);
            setAsFailed();
        }
    }

    @SuppressWarnings("unused")
    @JsonProperty("targetCardIds")
    private Collection<PhysicalCard> getTargetCards() {
        return _cardsToPlace;
    }
}