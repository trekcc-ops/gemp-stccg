package com.gempukku.stccg.actions.playcard;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.decisions.ArbitraryCardsSelectionDecision;
import com.gempukku.stccg.decisions.CardsSelectionDecision;
import com.gempukku.stccg.filters.Filter;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.Player;
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

    public DownloadCardAction(Zone fromZone, Player player, Filterable playableCardFilter) {
        super(player, "Download card from " + fromZone.getHumanReadable(),
                ActionType.DOWNLOAD_CARD);
        _filter = Filters.and(playableCardFilter);
        _fromZone = fromZone;
    }

    @JsonIgnore
    protected Collection<PhysicalCard> getPlayableCards(GameState gameState) {
        List<PhysicalCard> sourceCards;
        if (_fromZone == Zone.HAND)
            sourceCards = gameState.getHand(_performingPlayerId);
        else if (_fromZone == Zone.DISCARD)
            sourceCards = gameState.getDiscard(_performingPlayerId);
        else if (_fromZone == Zone.DRAW_DECK)
            sourceCards = gameState.getDrawDeck(_performingPlayerId);
        else throw new RuntimeException(
                "Error in ChooseAndPlayCardFromZoneEffect processing for zone " + _fromZone.getHumanReadable());

        return Filters.filter(sourceCards, gameState.getGame(), _filter, Filters.playable);
    }

    protected void playCard(final PhysicalCard selectedCard) {
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
                    !getPlayableCards(cardGame.getGameState()).isEmpty();
        else
            return !getPlayableCards(cardGame.getGameState()).isEmpty();
    }

    @Override
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException {
        Player performingPlayer = cardGame.getPlayer(_performingPlayerId);
        GameState gameState = cardGame.getGameState();
        Collection<PhysicalCard> playableCards = getPlayableCards(gameState);
        if (_fromZone == Zone.DISCARD || _fromZone == Zone.DRAW_DECK) {
            int minimum = _fromZone == Zone.DISCARD ? 1 : 0;
            cardGame.getUserFeedback().sendAwaitingDecision(
                    new ArbitraryCardsSelectionDecision(performingPlayer, "Choose a card to play",
                            new LinkedList<>(playableCards), minimum, 1, cardGame) {
                        @Override
                        public void decisionMade(String result) throws DecisionResultInvalidException {
                            List<PhysicalCard> selectedCards = getSelectedCardsByResponse(result);
                            if (!selectedCards.isEmpty()) {
                                final PhysicalCard selectedCard = selectedCards.getFirst();
                                playCard(selectedCard);
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
                            final PhysicalCard selectedCard = getSelectedCardsByResponse(result).iterator().next();
                            playCard(selectedCard);
                        }
                    });
        }
        return null;
    }

    protected Action getPlayCardAction() { return _playCardAction; }
    protected void setPlayCardAction(Action action) { _playCardAction = action; }

}