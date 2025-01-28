package com.gempukku.stccg.actions.playcard;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.decisions.ArbitraryCardsSelectionDecision;
import com.gempukku.stccg.decisions.CardsSelectionDecision;
import com.gempukku.stccg.filters.Filter;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.*;
import com.gempukku.stccg.gamestate.GameState;
import com.gempukku.stccg.modifiers.ModifierFlag;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class DownloadCardAction extends ActionyAction {
    private final Filter _filter;

    @JsonProperty("playCardAction")
    @JsonIdentityReference(alwaysAsId = true)
    private Action _playCardAction;

    @JsonProperty("originZone")
    private final Zone _fromZone;

    public DownloadCardAction(DefaultGame cardGame, Zone fromZone, Player player, Filterable playableCardFilter) {
        super(cardGame, player, "Download card from " + fromZone.getHumanReadable(),
                ActionType.DOWNLOAD_CARD);
        _filter = Filters.and(playableCardFilter);
        _fromZone = fromZone;
    }


    @JsonIgnore
    protected Collection<PhysicalCard> getPlayableCards(DefaultGame cardGame, GameState gameState) {
        try {
            List<PhysicalCard> sourceCards;
            Player performingPlayer = gameState.getPlayer(_performingPlayerId);
            if (_fromZone == Zone.HAND)
                sourceCards = performingPlayer.getCardsInHand();
            else if (_fromZone == Zone.DISCARD)
                sourceCards = performingPlayer.getCardsInGroup(Zone.DISCARD);
            else if (_fromZone == Zone.DRAW_DECK)
                sourceCards = performingPlayer.getCardsInDrawDeck();
            else throw new RuntimeException(
                        "Error in ChooseAndPlayCardFromZoneEffect processing for zone " + _fromZone.getHumanReadable());

            return Filters.filter(sourceCards, cardGame, _filter, Filters.playable);
        } catch(PlayerNotFoundException exp) {
            cardGame.sendErrorMessage(exp);
            return new LinkedList<>();
        }
    }


    protected void playCard(final PhysicalCard selectedCard) throws InvalidGameLogicException {
        _playCardAction = selectedCard.getPlayCardAction(true);
        selectedCard.getGame().getActionsEnvironment().addActionToStack(_playCardAction);
    }

    @Override
    public boolean wasCarriedOut() {
        if (_playCardAction == null)
            return false;
        if (_playCardAction instanceof PlayCardAction)
            return _playCardAction.wasCarriedOut();
        return true;
    }

    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        if (_fromZone == Zone.DISCARD || _fromZone == Zone.DRAW_DECK)
            return !cardGame.getModifiersQuerying().hasFlagActive(ModifierFlag.CANT_PLAY_FROM_DISCARD_OR_DECK) &&
                    !getPlayableCards(cardGame, cardGame.getGameState()).isEmpty();
        else
            return !getPlayableCards(cardGame, cardGame.getGameState()).isEmpty();
    }

    @Override
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException, PlayerNotFoundException {
        Player performingPlayer = cardGame.getPlayer(_performingPlayerId);
        GameState gameState = cardGame.getGameState();
        Collection<PhysicalCard> playableCards = getPlayableCards(cardGame, gameState);
        if (_fromZone == Zone.DISCARD || _fromZone == Zone.DRAW_DECK) {
            int minimum = _fromZone == Zone.DISCARD ? 1 : 0;
            cardGame.getUserFeedback().sendAwaitingDecision(
                    new ArbitraryCardsSelectionDecision(performingPlayer, "Choose a card to play",
                            new LinkedList<>(playableCards), minimum, 1, cardGame) {
                        @Override
                        public void decisionMade(String result) throws DecisionResultInvalidException {
                            try {
                                List<PhysicalCard> selectedCards = getSelectedCardsByResponse(result);
                                if (!selectedCards.isEmpty()) {
                                    final PhysicalCard selectedCard = selectedCards.getFirst();
                                    playCard(selectedCard);
                                }
                            } catch(InvalidGameLogicException exp) {
                                throw new DecisionResultInvalidException(exp.getMessage());
                            }
                        }
                    }); // Additional implementations are for playing from hand
        } else if (playableCards.size() == 1) {
            playCard(playableCards.iterator().next());
        } else if (playableCards.size() > 1) {
            cardGame.getUserFeedback().sendAwaitingDecision(
                    new CardsSelectionDecision(performingPlayer, "Choose a card to play", playableCards,
                            1, 1, cardGame) {
                        @Override
                        public void decisionMade(String result) throws DecisionResultInvalidException {
                            try {
                                final PhysicalCard selectedCard = getSelectedCardsByResponse(result).iterator().next();
                                playCard(selectedCard);
                            } catch(InvalidGameLogicException exp) {
                                throw new DecisionResultInvalidException(exp.getMessage());
                            }
                        }
                    });
        }
        setAsSuccessful();
        return null;
    }

    protected Action getPlayCardAction() { return _playCardAction; }
    protected void setPlayCardAction(Action action) { _playCardAction = action; }

}